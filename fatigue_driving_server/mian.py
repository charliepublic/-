import socket
import time
import numpy as np
import cv2

from io import BytesIO
from multiprocessing import Process
from multiprocessing import Queue
from PIL import Image

from detect import eyes_detect


def server_run(queue, host):
    while True:

        # Start time
        start = time.time()

        new_host, addr = host.accept()  # 初始化
        size = new_host.recv(10)
        size_str = str(size, encoding="utf-8")  # 将Byte流转string
        size_str = size_str.strip()
        file_size = int(size_str)  # 获取图片文件大小
        temp = bytearray()
        has_size = 0  # 已接收数据大小
        while True:
            if file_size == has_size:  # 如果接收的数据足够
                break
            res_data = file_size - has_size
            if res_data >= 1024:
                data = new_host.recv(1024)
            else:
                data = new_host.recv(res_data)
            temp = temp + data
            data_size = len(data)
            has_size = has_size + data_size
        new_host.close()  # 传输结束

        # 图像处理
        image = Image.open(BytesIO(temp))
        img = cv2.cvtColor(np.asarray(image), cv2.COLOR_RGB2BGR)

        queue.put(img)
        # cv2.imshow("Frame", img)
        # cv2.waitKey(1)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("Estimated second : {0}".format(seconds))


def eyes_detect_func(queue, detect):
    while True:
        # Start time
        start = time.time()

        detect.detect_picture(queue.get(block=True))
        # cv2.imshow("Frame", queue.get(block=True))
        # cv2.waitKey(1)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        print("total : {0}".format(seconds))
        print("\n")


if __name__ == '__main__':
    detect = eyes_detect()
    host = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # 绑定端口:
    host.bind(('192.168.1.14', 9999))
    print('Bind TCP on 9999...')
    host.listen(20)

    queue = Queue(2000)

    process_sever = Process(target=server_run, args=(queue, host,))
    process_detect = Process(target=eyes_detect_func, args=(queue, detect,))
    process_sever.start()
    process_detect.start()
    process_sever.join()
    process_detect.join()

    host.close()
