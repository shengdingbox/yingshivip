package cn.dabaotv.movie.main.home;

import cn.dabaotv.movie.main.list.DLBlock;
import cn.dabaotv.movie.view.list.ItemList;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 幻陌
 * Home 内容数据
 */

public class DLMainHomeConl {
    public String name; // 分类名
    public List<ItemList> slide;  // 幻灯片
    public List<DLBlock> video; // 内容子分类数据
    public DLMainHomeConl(){
        name = "";
        slide = new ArrayList<>();
        video = new ArrayList<>();
    }
}
