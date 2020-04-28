package com.example.telegraph;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageButton btnSend;
    EditText edtMsgTxt;
    RecyclerView chatWindow;
    private String myName = "Путин";
    private MessageController controller;
    protected TextView txtCounter;

    protected final boolean MESSAGE_IN = false;
    protected final boolean MESSAGE_OUT = true;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        btnSend=findViewById(R.id.btnSend);
        edtMsgTxt=findViewById(R.id.editTxtMsg);
        chatWindow=findViewById(R.id.chatWndw);
        txtCounter=findViewById(R.id.counter);

        controller=new MessageController();
        controller
                .setIncomingLayout(R.layout.msg_layout_incom)
                .setOutgoingLayout(R.layout.msg_layout)
              //  .setIncomingLayout(R.layout.incoming_message)
              //  .setOutgoingLayout(R.layout.outgoing_message)
                .setMessageTextId(R.id.txtMsg)
                .setUserNameId(R.id.txtName)
                .setMessageTimeId(R.id.txtDate)
                .appendTo(chatWindow,this);

        controller.addMessage(new MessageController.Message("От глаз администратора не скроешься","Администратор",MESSAGE_OUT));
        controller.addMessage(new MessageController.Message("Ok, но в более длинной форме, чтобы не париться с выравниванием","Не администратор",MESSAGE_IN));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edtMsgTxt.getText().toString();
                controller.addMessage(new MessageController.Message(text,myName,MESSAGE_OUT));
                edtMsgTxt.setText("");
                server.sendMsg(text);
            //    controller.addMessage(new MessageController.Message("угу, но в более длинной форме, чтобы не париться с выравниванием","Не Путин",MESSAGE_OUT));

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> srvPair) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controller.addMessage(new MessageController.Message(srvPair.second, srvPair.first, MESSAGE_IN));
                    }
                });
            }
        },
                new Consumer<Pair<String, Integer>>() {
                    @Override
                    public void accept(final Pair<String, Integer> srvPair) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String name=srvPair.first;
                                Integer cnt=srvPair.second;
                                if (name!="") {
                                    showToast(name);
                                };
                                txtCounter.setText(cnt+" users connected.");
                            }
                        });
                    }
                }
        );
        server.connect();
        //Получим Имя юзера
        //взято с гита и работает, пусть так будет
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Who are you?");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myName=input.getText().toString();
                        server.sendName(myName);
                    }
                });
        builder.show();

    }
    private void showToast(String name) {
        CharSequence text = "User "+name+" just connected";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }
    @Override
    protected void onStop() {
        super.onStop();
        server.disconnect();
    }
}
