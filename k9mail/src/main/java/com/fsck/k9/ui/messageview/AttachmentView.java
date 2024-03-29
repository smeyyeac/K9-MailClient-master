package com.fsck.k9.ui.messageview;


import android.content.Context;
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
import com.fsck.k9.NewClasslar.ViewDialog;
import com.fsck.k9.R;
import com.fsck.k9.helper.SizeFormatter;
import com.fsck.k9.mail.OpenPGP.OpenPGPEncryptDecrypt;
import com.fsck.k9.mail.Signature.OpenPGPSignature;
import com.fsck.k9.mailstore.AttachmentViewInfo;
import com.fsck.k9.mailstore.MessageViewInfoExtractor;
import com.fsck.k9.view.MessageHeader;


public class AttachmentView extends FrameLayout implements OnClickListener, OnLongClickListener {
    private AttachmentViewInfo attachment;
    private AttachmentViewCallback callback;

    private Button viewButton;
    private Button downloadButton;
    private Button msignatureResult;
    public static String signaturuResult = null;
    public static String message = "";

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
        String signaruteResult = MessageViewInfoExtractor.signaruteResult();
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
        setAttachmentSize(attachment.size);
        refreshThumbnail();

        //İmzalama icon çağırma
/*
        signaturuResult = MessageViewInfoExtractor.signaruteResult();
        Log.e("Getir attachment", signaturuResult);
        if(signaturuResult.equals("true")){
            MessageHeader.tikYap();

        }else if(signaturuResult.equals("false")){
            MessageHeader.carpiYap();

        }*/
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

}
