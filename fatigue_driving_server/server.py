from io import BytesIO

import cv2
import numpy as np
from PIL import Image


class server_process():

    def run(self,host):
        while True:
            new_host, addr = host.accept() #初始化
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

    def get_image(self, img):
        return img
        pass