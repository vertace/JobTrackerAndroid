package com.tt.helpers;

import com.tt.enumerations.ServerResult;

public class AsycResponse {
    public interface AsyncResponse {
        void processFinish(ServerResult result);
    }
}
