package edu.cs4730.wearappvoice.voice;

import java.util.List;

import edu.cs4730.wearappvoice.utils.Constants;
import edu.cs4730.wearappvoice.utils.Context;

public class MessageAdapter {
    private List<edu.cs4730.wearappvoice.utils.Message> messages;
    private Context context;

    public MessageAdapter(Context context, List<edu.cs4730.wearappvoice.utils.Message> messages) {
        this.messages = messages;
        this.context = context;
    }


    private void playVoiceMessage(String voicefile) {

        edu.cs4730.wearappvoice.utils.Message msg = new edu.cs4730.wearappvoice.utils.Message();
        msg.what = Constants.MESSAGE_SET_PLAY_VOICE;
        msg.obj = voicefile;
        Constants.mainHandle.sendMessage(msg);
    }

}
