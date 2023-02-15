package com.mk.m_folder.data;

import java.io.File;
import java.util.List;

public class StorageFiles {

    private List<File> properFiles;
    private List<File> otherFiles;

    public StorageFiles(List<File> properFiles, List<File> otherFiles) {
        this.properFiles = properFiles;
        this.otherFiles = otherFiles;
    }

    public List<File> getProperFiles() {
        return properFiles;
    }

    public void setProperFiles(List<File> properFiles) {
        this.properFiles = properFiles;
    }

    public List<File> getOtherFiles() {
        return otherFiles;
    }

    public void setOtherFiles(List<File> otherFiles) {
        this.otherFiles = otherFiles;
    }
}
