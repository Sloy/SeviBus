package com.sloy.sevibus.resources.actions.user;

import com.firebase.client.Firebase;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;

public class LogOutActionTest {

    @Mock
    UserDataSource userDataSource;
    @Mock
    Firebase firebase;

    private LogOutAction action;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new LogOutAction(userDataSource, firebase);
        when(userDataSource.removeCurrentUser()).thenReturn(empty());
    }

    @Test
    public void logouts_from_datasource() throws Exception {
        action.logOut()
          .toBlocking().subscribe();

        verify(userDataSource).removeCurrentUser();
    }

    @Test
    public void logouts_from_firebase() throws Exception {
        action.logOut()
          .toBlocking().subscribe();

        verify(firebase).unauth();
    }


}