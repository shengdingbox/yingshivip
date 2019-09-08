package cn.dabaotv.movie.screen;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.qingfeng.clinglibrary.listener.BrowseRegistryListener;
import com.qingfeng.clinglibrary.service.ClingUpnpService;
import com.qingfeng.clinglibrary.service.manager.ClingManager;
import com.qingfeng.clinglibrary.service.manager.DeviceManager;

public class SearchScreenUtil {
    private static final String TAG = "SearchScreenUtil";

    /** 用于监听发现设备 */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");

            ClingManager.getInstance().setUpnpService(null);
        }
    };
}
