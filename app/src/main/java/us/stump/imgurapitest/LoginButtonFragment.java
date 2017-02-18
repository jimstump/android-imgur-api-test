package us.stump.imgurapitest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} that includes a "Login to Imgur" button.
 * This fragment is used as a sort of "Welcome" screen for the app.
 * Activities that contain this fragment must implement the
 * {@link OnLoginButtonClickedListener} interface
 * to handle interaction events.
 */
public class LoginButtonFragment extends Fragment implements View.OnClickListener {

    /**
     * The login button.
     */
    private Button login_button;

    /**
     * Instance of the class that will receive the tap/click event for the login button.
     */
    private OnLoginButtonClickedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoginButtonFragment() {
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

    @Override
    public void onResume() {
        super.onResume();

        // we invalidate the options menu to make sure that the displayed options are relevant to this view
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Receive our button clicks
     * @param v The view that was clicked
     */
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

    /**
     * Handle the click/tap event for the login button.
     *
     * This really just invokes the action on our OnLoginButtonClickedListener object.
     */
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

        if (login_button != null) {
            login_button.setOnClickListener(null);
            login_button = null;
        }
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
        /**
         * Handle the click/tap event for the login button.
         */
        void onLoginButtonClicked();
    }
}
