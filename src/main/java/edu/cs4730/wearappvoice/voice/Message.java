package edu.cs4730.wearappvoice.voice;


import java.util.List;

import edu.cs4730.wearappvoice.utils.Constants;

import javax.swing.text.html.ListView;

public class Message {
    public String text;
    public boolean isUser;
    public boolean isMusic;
    public boolean isNavigation;
    public String songTitle;
    public String artistName;
    public String audioUrl;
    public String voicefile;

    public Message(String text, boolean isUser, boolean isMusic, boolean isNavigation, String filaname) {
        this.text = text;
        this.isUser = isUser;
        this.isMusic = isMusic;
        this.isNavigation = isNavigation;
        this.voicefile = filaname;
    }

    public Message(String text, boolean isUser, boolean isMusic, boolean isNavigation, String songTitle, String artistName, String audioUrl) {
        this.text = text;
        this.isUser = isUser;
        this.isMusic = isMusic;
        this.isNavigation = isNavigation;
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.audioUrl = audioUrl;
    }

    public void AddAndUpdateList()
    {
        Constants.dialogMessage.add(this);

        edu.cs4730.wearappvoice.utils.Message msg = new edu.cs4730.wearappvoice.utils.Message();
        msg.what = Constants.MESSAGE_UPDATE_MSGLIST;
        if(Constants.debugHandle != null)
            Constants.debugHandle.sendMessage(msg);
    }

    public void AddAndUpdateList(MessageAdapter messageAdapter, List<Message> messageList, ListView messageListView)
    {
        messageList.add(this);

    }
}
