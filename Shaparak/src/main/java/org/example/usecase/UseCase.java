package org.example.usecase;

public interface UseCase <REQUEST, RESPONSE> {

    RESPONSE execute(REQUEST request);

}
