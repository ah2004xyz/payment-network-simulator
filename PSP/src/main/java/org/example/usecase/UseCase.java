package org.example.usecase;

public interface UseCase<REQUEST, RESPONSE> {

    public RESPONSE execute(REQUEST request);

}
