package com.sloy.sevibus.resources.actions.user;

import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloydev.gallego.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ObtainUserActionTest {

    private static final SevibusUser STUB_USER = new SevibusUser();

    @Mock
    UserDataSource userDataSource;
    @Mock
    CrashReportingTool crashReportingTool;

    private ObtainUserAction obtainCurrentUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        obtainCurrentUser = new ObtainUserAction(userDataSource, crashReportingTool);
    }

    @Test
    public void returns_user_in_datasource_when_present() throws Exception {
        when(userDataSource.getCurrentUser()).thenReturn(Observable.just(STUB_USER));

        Optional<SevibusUser> result = obtainCurrentUser.obtainUser().toBlocking().single();

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(STUB_USER);
    }

    @Test
    public void returns_absent_when_user_not_in_datasource() throws Exception {
        when(userDataSource.getCurrentUser()).thenReturn(Observable.empty());

        Optional<SevibusUser> result = obtainCurrentUser.obtainUser().toBlocking().single();

        assertThat(result.isPresent()).isFalse();
    }


    @Test
    public void associate_user_with_crash_reporting_tool() throws Exception {
        when(userDataSource.getCurrentUser()).thenReturn(Observable.just(STUB_USER));

        obtainCurrentUser.obtainUser()
          .toBlocking().single();

        verify(crashReportingTool).associateUser(any(SevibusUser.class));
    }

}