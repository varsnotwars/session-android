package org.thoughtcrime.securesms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.List;

import network.loki.messenger.R;

public class TransportOptionsAdapter extends BaseAdapter {

  private final LayoutInflater inflater;

  private List<TransportOption> enabledTransports;

  public TransportOptionsAdapter(@NonNull Context context,
                                 @NonNull List<TransportOption> enabledTransports)
  {
    super();
    this.inflater          = LayoutInflater.from(context);
    this.enabledTransports = enabledTransports;
  }

  public void setEnabledTransports(List<TransportOption> enabledTransports) {
    this.enabledTransports = enabledTransports;
  }

  @Override
  public int getCount() {
    return enabledTransports.size();
  }

  @Override
  public Object getItem(int position) {
    return enabledTransports.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.transport_selection_list_item, parent, false);
    }

    TransportOption transport   = (TransportOption) getItem(position);
    ImageView       imageView   = ViewUtil.findById(convertView, R.id.icon);
    TextView        textView    = ViewUtil.findById(convertView, R.id.text);
    TextView        subtextView = ViewUtil.findById(convertView, R.id.subtext);

    imageView.setImageResource(R.drawable.ic_arrow_up_circle_24);
    textView.setText(transport.getDescription());

    if (transport.getSimName().isPresent()) {
      subtextView.setText(transport.getSimName().get());
      subtextView.setVisibility(View.VISIBLE);
    } else {
      subtextView.setVisibility(View.GONE);
    }

    return convertView;
  }
}
