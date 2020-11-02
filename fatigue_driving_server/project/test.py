# import cv2
#
# # rtmp_str = "rtmp://58.200.131.2:1935/livetv/hunantv"
# rtmp_str = "rtmp://192.168.1.14:1935/live/home"
# cap = cv2.VideoCapture(rtmp_str)
# fps = cap.get(cv2.CAP_PROP_FPS)
# ret, image = cap.read()
# while True:
#     cv2.imshow('video', image)
#     cv2.waitKey(1)
#     ret, image = cap.read()


# from numba import cuda
# print(cuda.gpus)

# from multiprocessing import Process
# from multiprocessing.dummy import Manager
# from multiprocessing import Queue
#
#
# def server_run(meditor):
#     while True:
#         print("ready")
#         print("现在是" + str(meditor.get()))
#
#
# def eyes_detect_func(meditor):
#
#     i = 0
#     # while True:
#     #     if i % 2:
#     #         sleep(100)
#     #         meditor.put(True)
#     #         # print(str(meditor.get_alert())+"\n")
#     #     else:
#     #         meditor.put(False)
#     #
#     #     i += 1


# if __name__ == '__main__':
#     queue = Queue(1)
#     process_sever = Process(target=server_run, args=(queue,))
#     process_detect = Process(target=eyes_detect_func, args=(queue,))
#     process_sever.start()
#     process_detect.start()
#     process_sever.join()
#     process_detect.join()


#
#
# def get():
#     print(gl.get_value())
#
#
# if __name__ == '__main__':
#     gl.set_value("123")
#
#     print(gl.get_value())
#     get()

# import database as db
#
# if __name__ == '__main__':
#     database = db.database_operation()
#     number = "12345678911"
#     result = database.select_by_bumber(number)
#     print(result)
#
#     response = ""
#     temp = result[1:]
#     for item in temp:
#         response = response + ','+str(item)
#     print(response[1:])

# def run(self, queue, address, phone_number):
#     try:
#         new_host, addr = self.host.accept()  # 初始化
#
#         # 获取唯一识别ID
#         phone = new_host.recv(15)
#         phone_str = str(phone, encoding="utf-8")
#         phone_str = phone_str.strip()
#         phone_number["phone"] = phone_str
#         print("test" + phone_str)
#
#         # 获取flag
#         flag = new_host.recv(1)
#         flag_str = str(flag, encoding="utf-8")
#
#         if flag_str == "0":
#
#             # 获取图片文件大小
#             print("ok")
#             size = new_host.recv(10)
#             size_str = str(size, encoding="utf-8")  # 将Byte流转string
#             size_str = size_str.strip()
#             file_size = int(size_str)
#
#             temp = bytearray()
#             has_size = 0  # 已接收数据大小
#             while True:
#                 if file_size == has_size:  # 如果接收的数据足够
#                     break
#                 res_data = file_size - has_size
#                 if res_data >= 1024:
#                     data = new_host.recv(1024)
#                 else:
#                     data = new_host.recv(res_data)
#                 temp = temp + data
#                 data_size = len(data)
#                 has_size = has_size + data_size
#             # 图像处理
#             image = Image.open(BytesIO(temp))
#             img = cv2.cvtColor(np.asarray(image), cv2.COLOR_RGB2BGR)
#
#             queue.put(img)
#             address.put(new_host)
#             cv2.imshow("translate", img)
#             cv2.waitKey(1)
#             print("down")
#         elif flag_str == "1":
#             print("quire :" + phone_str)
#             database = db.database_operation()
#             database.refresh(phone_str)
#             result = database.select_by_bumber(phone_str)
#             response = ""
#             temp = result[1:]
#             for item in temp:
#                 response = response + ',' + str(item)
#             msg = response[1:]
#             print(msg)
#             new_host.send(msg.encode(encoding='utf-8'))
#         else:
#             return
#     except:
#         return
#
#     def is_right(self):
#         if self.pupils_located:
#             return self.horizontal_ratio() <= 0.35
#
#     def is_left(self):
#         if self.pupils_located:
#             return self.horizontal_ratio() >= 0.65
#
#     def is_center(self):
#         if self.pupils_located:
#             return self.is_right() is not True and self.is_left() is not True

