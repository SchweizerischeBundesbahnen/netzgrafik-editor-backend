package ch.sbb.pfi.netzgrafikeditor.integrationtest.helper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserHelper {
    public static RequestPostProcessor user(String sub, String... authorityNames) {
        Collection<GrantedAuthority> authorities =
                Arrays.stream(authorityNames)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        return jwt().authorities(authorities).jwt(jwt -> jwt.claim("sub", sub));
    }
}
