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
from time import sleep

from multiprocessing import Process
from multiprocessing.dummy import Manager
from multiprocessing import Queue


def server_run(meditor):
    while True:
        print("ready")
        print("现在是" + str(meditor.get()))


def eyes_detect_func(meditor):

    i = 0
    # while True:
    #     if i % 2:
    #         sleep(100)
    #         meditor.put(True)
    #         # print(str(meditor.get_alert())+"\n")
    #     else:
    #         meditor.put(False)
    #
    #     i += 1


if __name__ == '__main__':
    queue = Queue(1)
    process_sever = Process(target=server_run, args=(queue,))
    process_detect = Process(target=eyes_detect_func, args=(queue,))
    process_sever.start()
    process_detect.start()
    process_sever.join()
    process_detect.join()
