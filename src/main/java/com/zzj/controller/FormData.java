package com.zzj.controller;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class FormData {
/*
    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    public String description;*/

    @RestForm("file")
    public FileUpload file;
}