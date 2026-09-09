package com.aman.backend.service;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class ExecutionSecurityPolicy {

	public URI validateBaseUrl(String baseUrl) {
		URI uri;
		try {
			uri = URI.create(baseUrl);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Target base URL is invalid.");
		}

		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("Only HTTP and HTTPS target URLs are allowed.");
		}

		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("Target base URL must include a valid host.");
		}

		String asciiHost = IDN.toASCII(host).toLowerCase(Locale.ROOT);
		if ("localhost".equals(asciiHost) || asciiHost.endsWith(".localhost")) {
			throw new IllegalArgumentException("Localhost targets are not allowed for server-side execution.");
		}

		for (InetAddress address : resolveAll(asciiHost)) {
			if (isUnsafeAddress(address)) {
				throw new IllegalArgumentException("Target host resolves to a private, local, metadata, or otherwise unsafe address.");
			}
		}

		return uri;
	}

	private InetAddress[] resolveAll(String host) {
		try {
			return InetAddress.getAllByName(host);
		} catch (UnknownHostException exception) {
			throw new IllegalArgumentException("Target host could not be resolved.");
		}
	}

	private boolean isUnsafeAddress(InetAddress address) {
		return address.isAnyLocalAddress()
				|| address.isLoopbackAddress()
				|| address.isLinkLocalAddress()
				|| address.isSiteLocalAddress()
				|| address.isMulticastAddress()
				|| isCloudMetadataAddress(address)
				|| isUniqueLocalIpv6(address);
	}

	private boolean isCloudMetadataAddress(InetAddress address) {
		byte[] bytes = address.getAddress();
		return address instanceof Inet4Address
				&& Byte.toUnsignedInt(bytes[0]) == 169
				&& Byte.toUnsignedInt(bytes[1]) == 254
				&& Byte.toUnsignedInt(bytes[2]) == 169
				&& Byte.toUnsignedInt(bytes[3]) == 254;
	}

	private boolean isUniqueLocalIpv6(InetAddress address) {
		if (!(address instanceof Inet6Address)) {
			return false;
		}
		int firstByte = Byte.toUnsignedInt(address.getAddress()[0]);
		return (firstByte & 0xfe) == 0xfc;
	}
}
