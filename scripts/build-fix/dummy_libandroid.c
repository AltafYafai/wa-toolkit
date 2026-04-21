#include <stdarg.h>
#include <stddef.h>
// Dummy libandroid.so / liblog.so to prevent loading the real ones
void ATrace_beginSection(const char* sectionName) {}
void ATrace_endSection() {}
int ATrace_isEnabled() { return 0; }
int ASharedMemory_create(const char *name, size_t size) { return -1; }
size_t ASharedMemory_getSize(int fd) { return 0; }
int __android_log_print(int prio, const char* tag, const char* fmt, ...) { return 0; }
void __android_log_vprint(int prio, const char* tag, const char* fmt, va_list ap) {}
int __android_log_buf_write(int bufid, int prio, const char* tag, const char* msg) { return 0; }
int __android_log_write(int prio, const char* tag, const char* msg) { return 0; }
void __android_log_assert(const char* cond, const char* tag, const char* fmt, ...) {}
