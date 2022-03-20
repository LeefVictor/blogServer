package com.zzj.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DBUtils {

    //组装in问号条件
    public static String packInCondition(Collection<?> cond) {
        Objects.requireNonNull(cond);
        List<String> res = new ArrayList<>(cond.size());
        cond.forEach(it -> res.add("?"));
        return String.join(",", res);

    }
}
