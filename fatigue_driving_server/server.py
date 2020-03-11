import socket
from io import BytesIO

from scipy.spatial import distance
from imutils import face_utils
import imutils
import numpy as np
import dlib
import PIL
import cv2


def eye_aspect_ratio(eye):
    A = distance.euclidean(eye[1], eye[5])
    B = distance.euclidean(eye[2], eye[4])
    C = distance.euclidean(eye[0], eye[3])
    eye_distance = (A + B) / (2.0 * C)
    return eye_distance


def detectPicture(img):

    flag = 0
    frame = imutils.resize(img, width=450)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    subjects = detect(gray, 0)
    for subject in subjects:
        shape = predict(gray, subject)
        shape = face_utils.shape_to_np(shape)  # converting to NumPy Array
        leftEye = shape[lStart:lEnd]
        rightEye = shape[rStart:rEnd]
        leftEAR = eye_aspect_ratio(leftEye)
        rightEAR = eye_aspect_ratio(rightEye)
        ear = (leftEAR + rightEAR) / 2.0
        print(ear)
        leftEyeHull = cv2.convexHull(leftEye)
        rightEyeHull = cv2.convexHull(rightEye)
        cv2.drawContours(frame, [leftEyeHull], -1, (0, 255, 0), 1)
        cv2.drawContours(frame, [rightEyeHull], -1, (0, 255, 0), 1)
        if ear < thresh:
            flag += 1
            print(flag)
            if flag >= frame_check:
                cv2.putText(frame, "****************ALERT!****************", (10, 30),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
                cv2.putText(frame, "****************ALERT!****************", (10, 325),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
        else:
            flag = 0
    cv2.imshow("Frame", frame)
    cv2.waitKey(1)

thresh = 0.25
frame_check = 10
detect = dlib.get_frontal_face_detector()
predict = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")  # Dat file is the crux of the code

(lStart, lEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["left_eye"]
(rStart, rEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["right_eye"]

host = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# 绑定端口:
host.bind(('192.168.1.14', 9999))

print('Bind TCP on 9999...')
host.listen(5)

while True:

    new_host, addr = host.accept()  # s是服务端的socket对象s1是接入的客户端socket对象

    size = new_host.recv(10)
    size_str = str(size, encoding="utf-8")  # 将Byte流转string
    size_str = size_str.strip()
    file_size = int(size_str)  # 获取图片文件大小
    temp = bytearray()
    has_size = 0  # 已接收数据大小
    while True:
        if file_size == has_size:  # 如果接收的数据足够
            has_size = 0
            break
        res_data = file_size - has_size
        if res_data >= 1024:
            data = new_host.recv(1024)
        else:
            data = new_host.recv(res_data)
        temp = temp + data
        data_size = len(data)
        has_size = has_size + data_size
    image = PIL.Image.open(BytesIO(temp))
    img = cv2.cvtColor(np.asarray(image), cv2.COLOR_RGB2BGR)
    # cv2.imshow("Frame", img)
    # cv2.waitKey(1)
    detectPicture(img)


new_host.close()
host.close()
