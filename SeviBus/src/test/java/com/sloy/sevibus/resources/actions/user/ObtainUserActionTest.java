package com.sloy.sevibus.resources.actions.user;

import com.google.common.base.Optional;
import com.sloy.sevibus.resources.Session;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ObtainUserActionTest {

    private static final SevibusUser STUB_USER = new SevibusUser();

    @Mock
    UserDataSource userDataSource;
    @Mock
    Session session;

    private ObtainUserAction obtainCurrentUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        obtainCurrentUser = new ObtainUserAction(userDataSource, session);
    }

    @Test
    public void returns_user_in_session_when_user_present() throws Exception {
        when(session.getUser()).thenReturn(Optional.of(STUB_USER));

        Optional<SevibusUser> result = obtainCurrentUser.obtainUser().toBlocking().single();

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(STUB_USER);
    }

    @Test
    public void returns_user_in_datasource_when_user_not_in_session() throws Exception {
        when(session.getUser()).thenReturn(Optional.absent());
        when(userDataSource.getCurrentUser()).thenReturn(Observable.just(STUB_USER));

        Optional<SevibusUser> result = obtainCurrentUser.obtainUser().toBlocking().single();

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(STUB_USER);
    }

    @Test
    public void returns_absent_when_user_not_in_session_or_datasource() throws Exception {
        when(session.getUser()).thenReturn(Optional.absent());
        when(userDataSource.getCurrentUser()).thenReturn(Observable.empty());

        Optional<SevibusUser> result = obtainCurrentUser.obtainUser().toBlocking().single();

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void stores_user_in_session_when_user_is_absent_in_session_and_exist_in_datasource() throws Exception {
        when(session.getUser()).thenReturn(Optional.absent());
        when(userDataSource.getCurrentUser()).thenReturn(Observable.just(STUB_USER));

        obtainCurrentUser.obtainUser().toBlocking().single();

        verify(session).setUser(STUB_USER);
    }

}