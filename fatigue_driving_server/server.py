import socket
import cv2
import numpy as np

host = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# 绑定端口:
host.bind(('127.0.0.1', 9999))

print('Bind UDP on 9999...')


while True:
    img, addr = host.recvfrom(1024*1024)
    print("get info")

    img = numpy.array(Image.open(buf))
    a = Image.open(buf)
    #a.show()
    cv2.imshow("receive", img)
    buf.close()
    if (cv2.waitKey(1) & 0xFF) == ord('q'):
        break
    else:
        continue
    cv2.destroyAllWindows
