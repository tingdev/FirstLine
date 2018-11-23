package kevin.com.firstline;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment {

    private String url;
    private WebView wv;

    public WebViewFragment(String url) {
        // Required empty public constructor
        this.url = url;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_web_view, container, false);
        wv = v.findViewById(R.id.mywebview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl(url);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wv != null) {
            wv.destroy();
        }
    }
}
