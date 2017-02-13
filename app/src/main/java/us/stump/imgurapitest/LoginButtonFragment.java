package us.stump.imgurapitest;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass that includes a "Login to Imgur" button.
 * Activities that contain this fragment must implement the
 * {@link OnLoginButtonClickedListener} interface
 * to handle interaction events.
 */
public class LoginButtonFragment extends Fragment implements View.OnClickListener {

    private Button login_button;
    private OnLoginButtonClickedListener mListener;

    public LoginButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup the onclick listener for the login button
        login_button = (Button) getActivity().findViewById(R.id.imgur_login_button);
        login_button.setOnClickListener(this);
    }


    public void onClick(final View v) { //check for what button is pressed
        switch (v.getId()) {
            case R.id.imgur_login_button:
                onLoginButtonPressed();
                break;
            default:
                Log.e("imgur", "Clicked on unknown button: "+v.getId());
                break;
        }
    }

    public void onLoginButtonPressed() {
        if (mListener != null) {
            mListener.onLoginButtonClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginButtonClickedListener) {
            mListener = (OnLoginButtonClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginButtonClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginButtonClickedListener {
        void onLoginButtonClicked();
    }
}
