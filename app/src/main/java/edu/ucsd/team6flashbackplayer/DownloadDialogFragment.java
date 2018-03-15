package edu.ucsd.team6flashbackplayer;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * DownloadDialogFragment
 *
 * responsible for initializing a dialog that prompts the use to enter a url to download
 * songs or albums.
 */
public class DownloadDialogFragment extends DialogFragment {


    private String title = "Download";

    public DownloadDialogFragment() {
        // Required empty public constructor
    }

    /**
     * set the dialog title
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * interface of download dialog's two buttons.
     */
    public interface DownloadDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    /**
     * Set the listeners for the download dialog.
     * @param savedInstanceState
     * @return AlertDialog object
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final EditText downloadURLin = getActivity().findViewById(R.id.download_url);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.download_dialog, null))
        // Add action buttons
                .setMessage(title)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(DownloadDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        mListener.onDialogNegativeClick(DownloadDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }



    // Use this instance of the interface to deliver action events
    DownloadDialogListener mListener;

    /**
     * Override the default onAttatch to set the button listeners.
     * @param context context this fragment attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DownloadDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /**
     * remove the listeners on detach.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
