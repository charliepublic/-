from io import BytesIO

import cv2
import numpy as np
from PIL import Image

from project import database as db


class server_process():
    def __init__(self, host):
        self.host = host

    def run(self, queue, address, phone_number):
        try:
            new_host, addr = self.host.accept()  # 初始化

            # 获取唯一识别ID
            phone = new_host.recv(15)
            phone_str = str(phone, encoding="utf-8")
            phone_str = phone_str.strip()
            phone_number["phone"] = phone_str
            print("test" + phone_str)

            # 获取flag
            flag = new_host.recv(1)
            flag_str = str(flag, encoding="utf-8")

            if flag_str == "0":

                # 获取图片文件大小
                print("ok")
                size = new_host.recv(10)
                size_str = str(size, encoding="utf-8")  # 将Byte流转string
                size_str = size_str.strip()
                file_size = int(size_str)

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
                # 图像处理
                image = Image.open(BytesIO(temp))
                img = cv2.cvtColor(np.asarray(image), cv2.COLOR_RGB2BGR)

                queue.put(img)
                address.put(new_host)
                cv2.imshow("translate", img)
                cv2.waitKey(1)
                print("down")
            elif flag_str == "1":
                print("quire :" + phone_str)
                database = db.database_operation()
                database.refresh(phone_str)
                result = database.select_by_bumber(phone_str)
                response = ""
                temp = result[1:]
                for item in temp:
                    response = response + ',' + str(item)
                msg = response[1:]
                print(msg)
                new_host.send(msg.encode(encoding='utf-8'))
            else:
                return
        except:
            return
