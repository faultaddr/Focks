package cn.panyunyi.focks.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import cn.panyunyi.focks.R;

public class EditMenuFragment extends DialogFragment {
    public String menuTitle;
    private MenuListener listener;

    public static EditMenuFragment newInstance() {
        return new EditMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_menu, null);
        Button btn = v.findViewById(R.id.edit_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(">>>", (listener == null) + "");
                listener.sendContent(((EditText) (v.getRootView().findViewById(R.id.edit_menu))).getText().toString());
            }
        });
        return v;
    }
    public void setToActivityListener(MenuListener listener){
        this.listener = listener;
    }
    public interface MenuListener {
        void sendContent(String info);
    }
}
