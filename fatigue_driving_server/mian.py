import socket
import time

from multiprocessing import Process
from multiprocessing import Queue


import server
from detect import eyes_detect


def server_run(queue, translate, notify):
    while True:
        # Start time
        start = time.time()

        translate.run(queue, notify)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("Estimated second : {0}".format(seconds))


def eyes_detect_func(queue, detect, notify):
    while True:
        # Start time
        start = time.time()

        detect.detect_picture(queue.get(block=True), notify)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("total : {0}".format(seconds))
        print("\n")


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

    host = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # 绑定端口:
    host.bind(('192.168.1.14', 9999))
    translate = server.server_process(host)

    print('Bind TCP on 9999...')
    host.listen(20)

    queue = Queue(2000)
    notify = Queue(2000)
    address = Queue(2000)
    process_sever = Process(target=server_run, args=(queue, translate, address,))
    process_detect = Process(target=eyes_detect_func, args=(queue, detect, notify,))
    process_response = Process(target=response, args=(address, notify,))
    process_sever.start()
    process_detect.start()
    process_response.start()
    process_sever.join()
    process_detect.join()
    process_response.join()

    host.close()
