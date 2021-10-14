package com.movtalent.app.presenter;

import com.movtalent.app.http.ApiService;
import com.movtalent.app.http.BaseApi;
import com.movtalent.app.model.dto.ParamDto;
import com.movtalent.app.model.dto.PostDto;

/**
 * @author xujunxiang
 * createTime 2020-3-12
 */
public class ParamPresenter {


    private IParam iParam;

    public ParamPresenter(IParam iParam) {
        this.iParam = iParam;
    }

    public void getParam() {
        BaseApi.request(BaseApi.createApi(ApiService.class)
                        .getParam(), new BaseApi.IResponseListener<ParamDto>() {
                    @Override
                    public void onSuccess(ParamDto data) {
                        iParam.loadParam(data);
                    }

                    @Override
                    public void onFail() {
                    }
                }
        );
    }


    public interface IParam {
        void loadParam(ParamDto dto);
    }
}
