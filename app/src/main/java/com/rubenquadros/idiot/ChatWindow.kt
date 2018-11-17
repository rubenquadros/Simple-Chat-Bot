package com.rubenquadros.idiot

import ai.api.AIDataService
import ai.api.AIListener
import ai.api.AIServiceException
import ai.api.android.AIConfiguration
import ai.api.android.AIService
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat_window.*


class ChatWindow : AppCompatActivity(), AIListener {

//    internal var recyclerView: RecyclerView
//    internal var editText: EditText
//    internal var addBtn: RelativeLayout
      internal lateinit var ref: DatabaseReference
//    internal var adapter: FirebaseRecyclerAdapter<ChatMessage, ShowMsg>
    internal var flagFab: Boolean? = true

    private var aiService: AIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_window)
        cleanSlate()
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)

//        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
//        editText = findViewById(R.id.editText) as EditText
//        addBtn = findViewById(R.id.addBtn) as RelativeLayout

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        ref = FirebaseDatabase.getInstance().reference
        ref.keepSynced(true)

        val config = AIConfiguration("00724589d60746dda84694ebb9fb6192",
                ai.api.AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System)

        aiService = AIService.getService(this, config)
        aiService!!.setListener(this)

        val aiDataService = AIDataService(config)

        val aiRequest = AIRequest()

        sendMsg.setOnClickListener {
            val message = typeMsg.text.toString().trim()

            if (message != "") {

                val chatMessage = ChatMessage(message, "user")
                ref.child("chat").push().setValue(chatMessage)

                aiRequest.setQuery(message)
                object : AsyncTask<AIRequest, Void, AIResponse>() {

                    override fun doInBackground(vararg aiRequests: AIRequest): AIResponse? {
                        val request = aiRequests[0]
                        try {
                            return aiDataService.request(aiRequest)
                        } catch (e: AIServiceException) {
                            Log.i("EXCEPTION", e.toString())
                        }

                        return null
                    }

                    override fun onPostExecute(response: AIResponse?) {
                        if (response != null) {

                            val result = response.result
                            val reply = result.fulfillment.speech
                            val chatMessage = ChatMessage(reply, "bot")
                            ref.child("chat").push().setValue(chatMessage)
                        }
                    }
                }.execute(aiRequest)
            } else {
                aiService!!.startListening()
            }

            typeMsg.setText("")
        }

//        typeMsg.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                val fab_img = findViewById(R.id.sendMsg) as ImageView
//                val img = BitmapFactory.decodeResource(resources, R.drawable.ic_send_black_24dp)
//                val img1 = BitmapFactory.decodeResource(resources, R.drawable.ic_mic_black_24dp)
//
//
//                if (s.toString().trim { it <= ' ' }.length != 0 && flagFab!!) {
//                    ImageViewAnimatedChange(this@ChatWindow, fab_img, img)
//                    flagFab = false
//
//                } else if (s.toString().trim { it <= ' ' }.length == 0) {
//                    ImageViewAnimatedChange(this@ChatWindow, fab_img, img1)
//                    flagFab = true
//
//                }
//
//
//            }
//
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })

        val adapter = object : FirebaseRecyclerAdapter<ChatMessage, ShowMsg>(ChatMessage::class.java, R.layout.message_layout, ShowMsg::class.java, ref.child("chat")) {
            override fun populateViewHolder(viewHolder: ShowMsg, model: ChatMessage, position: Int) {

                if (model.msgUser.equals("user")) {

                    viewHolder.rightText.setText(model.msgText)

                    viewHolder.rightText.setVisibility(View.VISIBLE)
                    viewHolder.leftText.setVisibility(View.GONE)
                } else {
                    viewHolder.leftText.setText(model.msgText)

                    viewHolder.rightText.setVisibility(View.GONE)
                    viewHolder.leftText.setVisibility(View.VISIBLE)
                }
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = adapter.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 && lastVisiblePosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                }
            }
        })
        recyclerView.adapter = adapter
    }

    private fun cleanSlate() {
        ref = FirebaseDatabase.getInstance().reference
        ref.child("chat").setValue(null)
    }

    fun ImageViewAnimatedChange(c: Context, v: ImageView, new_image: Bitmap) {
        val anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out)
        val anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                v.setImageBitmap(new_image)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                v.startAnimation(anim_in)
            }
        })
        v.startAnimation(anim_out)
    }

    override fun onResult(response: ai.api.model.AIResponse) {


        val result = response.result

        val message = result.resolvedQuery
        val chatMessage0 = ChatMessage(message, "user")
        ref.child("chat").push().setValue(chatMessage0)

        val reply = result.fulfillment.speech
        val chatMessage = ChatMessage(reply, "bot")
        ref.child("chat").push().setValue(chatMessage)


    }

    override fun onError(error: ai.api.model.AIError) {

    }

    override fun onAudioLevel(level: Float) {

    }

    override fun onListeningStarted() {

    }

    override fun onListeningCanceled() {

    }

    override fun onListeningFinished() {

    }
}