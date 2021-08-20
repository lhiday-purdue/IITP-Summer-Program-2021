import sys
import msvcrt


def get_secret(message=None) -> str:
    if message is not None:
        sys.stdout.write(message)
        sys.stdout.flush()
    buf = bytearray()
    skip = False
    while True:
        if not msvcrt.kbhit():
            continue
        x = msvcrt.getch()
        if x.startswith(b"\xe0") or x.startswith(b"\x00"):
            skip = True
            continue
        if skip:
            skip = False
            continue
        t = int.from_bytes(x, sys.byteorder)
        if t == 13:
            msvcrt.putch('\n'.encode())
            return buf.decode()
        elif t == 8:
            if buf:
                sys.stdout.write("\b \b")
                sys.stdout.flush()
                buf.pop()
        elif 32 <= t < 126:
            msvcrt.putch('*'.encode())
            buf.append(t)
