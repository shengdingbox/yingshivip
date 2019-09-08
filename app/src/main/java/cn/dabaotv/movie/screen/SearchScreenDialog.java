package cn.dabaotv.movie.screen;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.dabaotv.video.R;
import com.qingfeng.clinglibrary.entity.ClingDevice;
import com.qingfeng.clinglibrary.entity.ClingDeviceList;
import com.qingfeng.clinglibrary.service.manager.ClingManager;
import com.qingfeng.clinglibrary.util.Utils;

import org.fourthline.cling.model.meta.Device;

import java.util.Collection;


public class SearchScreenDialog extends Dialog {
    private ListView listView;
    private DevicesAdapter devicesAdapter;
    private OnDeviceItemClickListener onDeviceItemClickListener;

    public SearchScreenDialog(@NonNull Context context) {
        this(context, R.style.Theme_Light_Dialog);
    }

    public SearchScreenDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        View view = View.inflate(context,R.layout.layout_search_screen,null);
        listView = view.findViewById(R.id.lv_devices);
        listView.setAdapter(devicesAdapter = new DevicesAdapter(context));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(onDeviceItemClickListener != null){
                    // 选择连接设备
                    ClingDevice item = devicesAdapter.getItem(position);
                    if (Utils.isNull(item)) {
                        onDeviceItemClickListener.onDeviceItemClick(SearchScreenDialog.this,false);
                        return;
                    }
                    Device device = item.getDevice();
                    if (Utils.isNull(device)) {
                        onDeviceItemClickListener.onDeviceItemClick(SearchScreenDialog.this,false);
                        return;
                    }
                    ClingManager.getInstance().setSelectedDevice(item);
                    onDeviceItemClickListener.onDeviceItemClick(SearchScreenDialog.this,true);
                }

            }
        });
        setContentView(view);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = dip2px(getContext(),300) ;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.RIGHT;
        getWindow().setAttributes(attributes);
        getWindow().setWindowAnimations(R.style.dialogStyle);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public void refreshDeviceList() {
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            devicesAdapter.clear();
            devicesAdapter.addAll(devices);
        }
    }

    public void onDeviceAdded(ClingDevice device){
        devicesAdapter.add(device);
    }

    public void onDeviceRemoved(ClingDevice device){
        devicesAdapter.remove(device);
    }

    public void setOnItemClick(OnDeviceItemClickListener onDeviceItemClickListener){
        this.onDeviceItemClickListener = onDeviceItemClickListener;
    }

    public void clearView(){
        devicesAdapter.clear();
    }


    public class DevicesAdapter extends ArrayAdapter<ClingDevice> {
        private LayoutInflater mInflater;

        public DevicesAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.devices_items, null);

            ClingDevice item = getItem(position);
            if (item == null || item.getDevice() == null) {
                return convertView;
            }

            Device device = item.getDevice();

            TextView textView = (TextView) convertView.findViewById(R.id.listview_item_line_one);
            textView.setText(device.getDetails().getFriendlyName());
            return convertView;
        }
    }

    public interface OnDeviceItemClickListener {
        void onDeviceItemClick(SearchScreenDialog dialog,boolean isActived);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
