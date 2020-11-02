import pymysql


class database_operation():
    def __init__(self):
        self.database = pymysql.connect(host='127.0.0.1'  # 连接名称，默认127.0.0.1
                                        , user='root'  # 用户名
                                        , passwd='980912'  # 密码
                                        , port=3306  # 端口，默认为3306
                                        , db='eyes'  # 数据库名称
                                        , charset='utf8'  # 字符编码
                                        )
        # print(self.database)
        self.center = "center_way"
        self.left = "left_way"
        self.right = "right_way"
        self.alert = "alert"
        self.cursor = self.database.cursor()

    def select_by_bumber(self, number):
        # 使用 execute() 方法执行 SQL 查询
        sql = "select * from info where phoneNumber = " + number
        print(sql)
        self.cursor.execute(sql)
        # 使用 fetchone() 方法.
        results = self.cursor.fetchone()  # 获取全部数据
        return results

    def insert(self, number):
        sql = "insert into info(phoneNumber) values(" + number + ")"
        print(sql)
        self.cursor.execute(sql)
        self.database.commit()

    def refresh(self, number):
        result = self.select_by_bumber(number)
        if result is None:
            self.insert(number)

    def update(self, number, label):
        sql = "update info set " + label + " = " + label + " + 1 " + "where phoneNumber = " + number
        print(sql)
        self.cursor.execute(sql)
        self.database.commit()

    def close_database(self):
        # 关闭游标，又从起始位置开始
        self.cursor.close()
        # 关闭数据库连接
        self.database.close()
