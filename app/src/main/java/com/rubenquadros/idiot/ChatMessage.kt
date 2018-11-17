package com.rubenquadros.idiot

class ChatMessage {

    var msgText: String? = null
    var msgUser: String? = null


    constructor(msgText: String, msgUser: String) {
        this.msgText = msgText
        this.msgUser = msgUser

    }

    constructor() {

    }
}