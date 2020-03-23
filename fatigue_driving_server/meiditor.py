class meditor():
    def __init__(self):
        alert = False
        Pakage = None

    def set_alert(self):
        self.alert = True

    def get_alert(self):
        return self.alert

    def clean(self):
        self.alert = False
