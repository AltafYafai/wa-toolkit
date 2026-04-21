package com.wa.toolkit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wa.toolkit.App
import com.wa.toolkit.xposed.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ThemeItem(
    val name: String,
    val author: String?,
    val isDefault: Boolean = false,
    val isActive: Boolean = false
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val rootDirectory = File(App.getWhatsappToolkitFolder(), "themes")
    
    private val _themes = MutableStateFlow<List<ThemeItem>>(emptyList())
    val themes: StateFlow<List<ThemeItem>> = _themes.asStateFlow()

    private val _currentThemeContent = MutableStateFlow("")
    val currentThemeContent: StateFlow<String> = _currentThemeContent.asStateFlow()

    private val repository = (application as App).preferenceRepository

    init {
        if (!rootDirectory.exists()) rootDirectory.mkdirs()
        loadThemes()
    }

    fun loadThemes() {
        viewModelScope.launch {
            val currentThemeName = repository.getStringValue("folder_theme", "Default Theme")
            val folderList = withContext(Dispatchers.IO) {
                rootDirectory.listFiles { file -> file.isDirectory }?.map { folder ->
                    val cssFile = File(folder, "style.css")
                    var author: String? = null
                    if (cssFile.exists()) {
                        val code = cssFile.readText(Charset.defaultCharset())
                        author = Utils.getAuthorFromCss(code)
                    }
                    ThemeItem(
                        name = folder.name,
                        author = author,
                        isActive = folder.name == currentThemeName
                    )
                } ?: emptyList()
            }

            val allThemes = mutableListOf(
                ThemeItem(
                    name = "Default Theme",
                    author = "System",
                    isDefault = true,
                    isActive = currentThemeName == "Default Theme"
                )
            )
            allThemes.addAll(folderList)
            _themes.value = allThemes
        }
    }

    fun selectTheme(theme: ThemeItem) {
        viewModelScope.launch {
            repository.setString("folder_theme", theme.name)
            val cssFile = File(rootDirectory, "${theme.name}/style.css")
            if (cssFile.exists()) {
                val code = cssFile.readText(Charset.defaultCharset())
                repository.setString("custom_css", code)
            } else {
                repository.setString("custom_css", "")
            }
            loadThemes()
        }
    }

    fun createTheme(name: String) {
        val newFolder = File(rootDirectory, name)
        if (!newFolder.exists()) {
            newFolder.mkdirs()
            val cssFile = File(newFolder, "style.css")
            cssFile.writeText("/* author = Your Name */\n\n/* Custom CSS for $name */")
            loadThemes()
        }
    }

    fun deleteTheme(name: String) {
        val folder = File(rootDirectory, name)
        if (folder.exists()) {
            folder.deleteRecursively()
            loadThemes()
        }
    }

    fun loadThemeContent(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cssFile = File(rootDirectory, "$name/style.css")
            if (cssFile.exists()) {
                _currentThemeContent.value = cssFile.readText(Charset.defaultCharset())
            } else {
                _currentThemeContent.value = ""
            }
        }
    }

    fun saveThemeContent(name: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cssFile = File(rootDirectory, "$name/style.css")
            cssFile.writeText(content, Charset.defaultCharset())
            
            // If this is the active theme, update custom_css preference too
            val currentThemeName = repository.getStringValue("folder_theme", "Default Theme")
            if (name == currentThemeName) {
                repository.setString(name = "custom_css", value = content)
            }
            withContext(Dispatchers.Main) {
                loadThemes()
            }
        }
    }

    fun importTheme(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<App>().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val zipInputStream = ZipInputStream(inputStream)
                    var zipEntry: ZipEntry?
                    
                    val zipFileName = getFileNameFromUri(uri).removeSuffix(".zip")

                    while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                        val entry = zipEntry!!
                        val entryName = entry.name
                        
                        val folderName: String
                        val targetPath: String

                        val lastSlashIndex = entryName.lastIndexOf('/')
                        if (lastSlashIndex > 0) {
                            folderName = entryName.substring(0, lastSlashIndex)
                            targetPath = entryName
                        } else {
                            folderName = zipFileName
                            targetPath = "$zipFileName/$entryName"
                        }

                        val newFolder = File(rootDirectory, folderName)
                        if (!newFolder.exists()) newFolder.mkdirs()
                        
                        if (entryName.endsWith("/")) continue
                        
                        val file = File(rootDirectory, targetPath)
                        Files.copy(zipInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
                withContext(Dispatchers.Main) {
                    loadThemes()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "theme_${System.currentTimeMillis()}"
        if (uri.scheme == "content") {
            getApplication<App>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                }
            }
        } else {
            name = uri.lastPathSegment ?: name
        }
        return name
    }
}
