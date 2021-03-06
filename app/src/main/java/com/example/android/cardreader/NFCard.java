/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.example.android.cardreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;

public final class NFCard extends Activity {
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private EditText board;
	private int sysVersion;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cardreader);
		board = (EditText) this.findViewById(R.id.editText1);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		//区分系统版本
		sysVersion = Integer.parseInt(VERSION.SDK);
		if(sysVersion<19)
			onNewIntent(getIntent());
	}


	@Override
	protected void onPause() {
		super.onPause();

		if (nfcAdapter != null){
			nfcAdapter.disableForegroundDispatch(this);
			disableReaderMode();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (nfcAdapter != null){
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardReader.FILTERS, CardReader.TECHLISTS);
			enableReaderMode();
		}
		Log.e("NFC----", IsoDep.class.getName());
		refreshStatus();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Log.d("NFCTAG", intent.getAction());
		board.setText((p != null) ? CardReader.load(p) : null);
	}


    @TargetApi(19) 
    private void enableReaderMode() {
    	if(sysVersion<19)
    		return;
    	
        int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        if (nfcAdapter != null) {
        	nfcAdapter.enableReaderMode(this, new MyReaderCallback(), READER_FLAGS, null);
        }
    }


    @TargetApi(19) 
    private void disableReaderMode() {
    	if(sysVersion<19)
    		return;
    	
        if (nfcAdapter != null) {
        	nfcAdapter.disableReaderMode(this);
        }
    }

    
	@TargetApi(19) 
	public class MyReaderCallback implements NfcAdapter.ReaderCallback {

		@Override
		public void onTagDiscovered(final Tag arg0) {
			NFCard.this.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	
	            	board.setText(CardReader.tagDiscovered(arg0));
	            }
	        });
		}
	}

    
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshStatus();
	}

	private void refreshStatus() {

		final String tip;
		if (nfcAdapter == null)
			tip = this.getResources().getString(R.string.tip_nfc_notfound);
		else if (nfcAdapter.isEnabled())
			tip = this.getResources().getString(R.string.tip_nfc_enabled);
		else
			tip = this.getResources().getString(R.string.tip_nfc_disabled);

		final StringBuilder s = new StringBuilder(
				this.getResources().getString(R.string.app_name));

		s.append("  --  ").append(tip);
		setTitle(s);
	}

}
