package com.fsck.k9.ui.messageview;


import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fsck.k9.K9;
import com.fsck.k9.NewClasslar.FileKey;
import com.fsck.k9.NewClasslar.OpenPGP;
import com.fsck.k9.R;
import com.fsck.k9.helper.SizeFormatter;
import com.fsck.k9.mail.Key.KeyOperation;
import com.fsck.k9.mail.OpenPGP.OpenPGPEncryptDecrypt;
import com.fsck.k9.mail.Signature.OpenPGPSignature;
import com.fsck.k9.mailstore.AttachmentViewInfo;
import com.fsck.k9.mailstore.MessageViewInfo;
import com.fsck.k9.mailstore.MessageViewInfoExtractor;

import java.io.File;

import static com.fsck.k9.mail.Signature.OpenPGPSignature.dogrula;



public class AttachmentView extends FrameLayout implements OnClickListener, OnLongClickListener {
    private AttachmentViewInfo attachment;
    private AttachmentViewCallback callback;

    private Button viewButton;
    private Button downloadButton;
    private static String messageTo;
    private static String encryptedMessage = "";
    private static String decryptedMessage = "";


    public AttachmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentView(Context context) {
        super(context);
    }

    public AttachmentViewInfo getAttachment() {
        return attachment;
    }

    public void enableButtons() {
        viewButton.setEnabled(true);
        downloadButton.setEnabled(true);
    }

    public void disableButtons() {
        viewButton.setEnabled(false);
        downloadButton.setEnabled(false);
    }

    public void setAttachment(AttachmentViewInfo attachment) {
        this.attachment = attachment;

        displayAttachmentInformation();
    }

    private void displayAttachmentInformation() {
        viewButton = (Button) findViewById(R.id.view);
        downloadButton = (Button) findViewById(R.id.download);

        if (attachment.size > K9.MAX_ATTACHMENT_DOWNLOAD_SIZE) {
            viewButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
        }

        viewButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        downloadButton.setOnLongClickListener(this);



        TextView attachmentName = (TextView) findViewById(R.id.attachment_name);
        attachmentName.setText(attachment.displayName);
        Log.w("Getir attaclar", attachment.displayName);
        if(attachment.displayName.equals("signature.asc")){
            onSaveButtonClick();
            dogrulama();
            FileKey.deleteStorageFile("Download","signature.asc");
        }else if(attachment.displayName.equals("encrypted.asc")){
            onSaveButtonClick();
            messageTo = MessageViewInfoExtractor.decryptTo();
            encryptedMessage = OpenPGPEncryptDecrypt.readDownloadFile("encrypted");
            Log.e("Getir encrypt", encryptedMessage );
            decryptedMessage = OpenPGPEncryptDecrypt.decrypted(messageTo, encryptedMessage);
            Log.e("Getir decrypt", decryptedMessage );
            int i=0;
            while(i<40000){
               i++;
            }
            FileKey.sifreliSil();
        }
        else{
            Log.e("Getir attachmentElse", attachment.displayName);
        }
        setAttachmentSize(attachment.size);

        refreshThumbnail();
    }

    private void setAttachmentSize(long size) {
        TextView attachmentSize = (TextView) findViewById(R.id.attachment_info);
        if (size == AttachmentViewInfo.UNKNOWN_SIZE) {
            attachmentSize.setText("");
        } else {
            String text = SizeFormatter.formatSize(getContext(), size);
            attachmentSize.setText(text);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view: {
                onViewButtonClick();
                break;
            }
            case R.id.download: {
                onSaveButtonClick();
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.download) {
            onSaveButtonLongClick();
            return true;
        }

        return false;
    }

    private void onViewButtonClick() {
        callback.onViewAttachment(attachment);
    }

    public  void onSaveButtonClick() {
        callback.onSaveAttachment(attachment);
    }

    private void onSaveButtonLongClick() {
        callback.onSaveAttachmentToUserProvidedDirectory(attachment);
    }

    public void setCallback(AttachmentViewCallback callback) {
        this.callback = callback;
    }

    public void refreshThumbnail() {
        ImageView thumbnailView = (ImageView) findViewById(R.id.attachment_icon);
        Glide.with(getContext())
                .load(attachment.internalUri)
                .placeholder(R.drawable.attached_image_placeholder)
                .centerCrop()
                .into(thumbnailView);
    }

    public void dogrulama(){
        String metin = MessageViewInfoExtractor.dogrulamaMetni();
        String mFrom = MessageViewInfoExtractor.dogrulamaFrom();
        Log.e("gelenmetin",metin);
        OpenPGPSignature.dogrula(metin, mFrom);
    }
}
