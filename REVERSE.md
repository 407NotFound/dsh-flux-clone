=== sections ===
There are 28 section headers, starting at offset 0x246550:

Section Headers:
  [Nr] Name              Type            Address          Off    Size   ES Flg Lk Inf Al
  [ 0]                   NULL            0000000000000000 000000 000000 00      0   0  0
  [ 1] .note.android.ident NOTE          0000000000000238 000238 000098 00   A  0   0  4
  [ 2] .note.gnu.build-id NOTE           00000000000002d0 0002d0 000024 00   A  0   0  4
  [ 3] .dynsym           DYNSYM          00000000000002f8 0002f8 000dc8 18   A  8   1  8
  [ 4] .gnu.version      VERSYM          00000000000010c0 0010c0 000126 02   A  3   0  2
  [ 5] .gnu.version_d    VERDEF          00000000000011e8 0011e8 000038 00   A  8   2  4
  [ 6] .gnu.version_r    VERNEED         0000000000001220 001220 000060 00   A  8   3  4
  [ 7] .gnu.hash         GNU_HASH        0000000000001280 001280 000020 00   A  3   0  8
  [ 8] .dynstr           STRTAB          00000000000012a0 0012a0 0007aa 00   A  0   0  1
  [ 9] .rela.dyn         RELA            0000000000001a50 001a50 014ef8 18   A  3   0  8
  [10] .rela.plt         RELA            0000000000016948 016948 000d50 18  AI  3  22  8
  [11] .rodata           PROGBITS        00000000000176a0 0176a0 01746c 00 AMS  0   0 16
  [12] .gcc_except_table PROGBITS        000000000002eb0c 02eb0c 000e24 00   A  0   0  4
  [13] .eh_frame_hdr     PROGBITS        000000000002f930 02f930 002d04 00   A  0   0  4
  [14] .eh_frame         PROGBITS        0000000000032638 032638 0116d8 00   A  0   0  8
  [15] .text             PROGBITS        0000000000043d10 043d10 1cbac8 00  AX  0   0 16
  [16] .plt              PROGBITS        000000000020f7e0 20f7e0 000900 00  AX  0   0 16
  [17] .data.rel.ro      PROGBITS        00000000002140e0 2100e0 0358b8 00  WA  0   0 16
  [18] .fini_array       FINI_ARRAY      0000000000249998 245998 000010 00  WA  0   0  8
  [19] .init_array       INIT_ARRAY      00000000002499a8 2459a8 000048 00  WA  0   0  8
  [20] .dynamic          DYNAMIC         00000000002499f0 2459f0 000210 10  WA  8   0  8
  [21] .got              PROGBITS        0000000000249c00 245c00 000100 00  WA  0   0  8
  [22] .got.plt          PROGBITS        0000000000249d00 245d00 000488 00  WA  0   0  8
  [23] .relro_padding    NOBITS          000000000024a188 246188 001e78 00  WA  0   0  1
  [24] .data             PROGBITS        000000000024e188 246188 0001d8 00  WA  0   0  8
  [25] .bss              NOBITS          000000000024e360 246360 002c30 00  WA  0   0 16
  [26] .comment          PROGBITS        0000000000000000 246360 0000cc 01  MS  0   0  1
  [27] .shstrtab         STRTAB          0000000000000000 24642c 000122 00      0   0  1
Key to Flags:
  W (write), A (alloc), X (execute), M (merge), S (strings), I (info),
  L (link order), O (extra OS processing required), G (group), T (TLS),
  C (compressed), x (unknown), o (OS specific), E (exclude),
  R (retain), y (purecode), p (processor specific)

=== dynamic symbols (JNI) ===
00000000001ed454 T JNI_OnLoad@@FLUX_1

=== JNI_OnLoad ===
00000000001ed454 T JNI_OnLoad@@FLUX_1

=== RegisterNatives refs ===
   43dbc:      	bl	0x43de4 <.text+0xd4>
   43de0:      	bl	0x20f840 <__stack_chk_fail@plt>
   43e24:      	bl	0x4520c <.text+0x14fc>
   43e5c:      	bl	0x20f850 <malloc@plt>
   43eb8:      	bl	0x20f830 <free@plt>
   43ec8:      	bl	0x20acbc <JNI_OnLoad+0x1d868>
   43eec:      	bl	0x20acbc <JNI_OnLoad+0x1d868>
   43efc:      	bl	0x20acbc <JNI_OnLoad+0x1d868>
   43f88:      	bl	0x20f860 <__memcpy_chk@plt>
   43f98:      	bl	0x20f870 <memcpy@plt>
   43fa8:      	bl	0x20f870 <memcpy@plt>
   44080:      	bl	0x20f840 <__stack_chk_fail@plt>
   44228:      	bl	0x4abac <.text+0x6e9c>
   44240:      	bl	0x4abac <.text+0x6e9c>
   44358:      	bl	0x20f890 <realloc@plt>
   44708:      	bl	0x20f890 <realloc@plt>
   44748:      	bl	0x20f8a0 <memset@plt>
   449a0:      	bl	0x4abac <.text+0x6e9c>
   44cd0:      	bl	0x20f8a0 <memset@plt>
   44cf4:      	bl	0x4abac <.text+0x6e9c>

=== strings: ui-ish (utf8) ===
memset
_ZTH16FluxImGuiContext
__memset_chk
pthread_key_delete
pthread_key_create
libfluxui.so
FLUX_1
KeypadEnter
Keypad1
Programmer error: %d visible items with conflicting ID!
Keypad7
KeypadMultiply
KeypadAdd
Keypad0
Keypad6
Keypad5
Keypad8
Keypad9
KeypadEqual
Keypad2
KeypadDecimal
Keypad4
KeypadDivide
Keypad3
KeypadSubtract
[io] %s: Key "%s" %s

=== strings: cyrillic ===
Android
r27c
12479018
/xCUjn2
__cxa_finalize
__cxa_atexit
__register_atfork
free
__stack_chk_fail
malloc
__memcpy_chk
memcpy
powf
realloc
memset
_ZTH16FluxImGuiContext
strlen
memchr
memmove
vsnprintf
fopen
fclose
ftell
fseek
fread
fwrite
qsort
fmodf
stdout
sscanf
fork
waitpid
execvp
exit
cosf
strcmp
fflush
printf
sincosf
acosf
