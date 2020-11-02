import multiprocessing
import socket
import time
from multiprocessing import Process
from multiprocessing import Queue

from project import server
from project.detect import eyes_detect


def server_run(queue, translate, notify, phone_number):
    while True:
        # Start time
        start = time.time()

        translate.run(queue, notify, phone_number)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("Estimated second : {0}".format(seconds))


def eyes_detect_func(queue, detect, notify, phone_number):
    while True:
        # Start time
        start = time.time()

        detect.detect_picture(queue.get(block=True), notify, phone_number)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("total : {0}".format(seconds))
        # print("\n")


def response(address_queue, notify_queue):
    while True:
        new_host = address_queue.get()
        present = notify_queue.get()
        if not present:
            msg = "false"
            new_host.send(msg.encode(encoding='utf-8'))
        else:
            msg = "true"
            new_host.send(msg.encode(encoding='utf-8'))
        new_host.close
        print("success")


if __name__ == '__main__':
    detect = eyes_detect()
    translate_host = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # 绑定端口1:
    translate_host.bind(('192.168.1.14', 9999))
    translate = server.server_process(translate_host)
    translate_host.listen(20)

    queue = Queue()
    notify = Queue()
    address = Queue()

    phone_number = multiprocessing.Manager().dict()
    process_sever = Process(target=server_run, args=(queue, translate, address, phone_number,))
    process_detect = Process(target=eyes_detect_func, args=(queue, detect, notify, phone_number,))
    process_response = Process(target=response, args=(address, notify,))

    process_sever.start()
    process_detect.start()
    process_response.start()

    process_sever.join()
    process_detect.join()
    process_response.join()
