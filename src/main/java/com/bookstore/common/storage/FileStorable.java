package com.bookstore.common.storage;


public interface FileStorable {

    String DELIMITER = "|";

    String toFileString();
}
