import cv2

# rtmp_str = "rtmp://58.200.131.2:1935/livetv/hunantv"
rtmp_str = "rtmp://192.168.1.14:1935/live/home"
cap = cv2.VideoCapture(rtmp_str)
fps = cap.get(cv2.CAP_PROP_FPS)
ret, image = cap.read()
while True:
    cv2.imshow('video', image)
    cv2.waitKey(1)
    ret, image = cap.read()