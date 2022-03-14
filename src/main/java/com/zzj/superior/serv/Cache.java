package com.zzj.superior.serv;

public interface Cache {

    Object read(String key);

    Object write(String key, Object obj);
}
