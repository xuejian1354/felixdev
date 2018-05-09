#ifndef ___SOCKS5___
#define ___SOCKS5___
#include <stdint.h>
 

#pragma pack(1)

struct Socks5 {
	enum {
		VERNO = 0x05,
		AUTH_METHOD_NON = 0x00,
		AUTH_METHOD_PWD = 0x02,
		AUTH_METHOD_FAILED = 0xFF,

		AUTH_PASSWORD_VER = 0x01,
		AUTH_PASSWORD_PASSED = 0x00,
		AUTH_PASSWORD_FAILED = 0x01,

		ADDRTYPE_IPV4 = 0x01,
		ADDRTYPE_DOMAIN = 0x03,

		CONN_SUCCEEDED = 0x00,
		CONN_SERVER_FAILURE = 0x01,
		CONN_NOT_ALLOWED = 0x02,
		CONN_NETWORK_UNREACHABLE = 0x03,
		CONN_HOST_UNREACHABLE = 0x04,
		CONN_CMD_NOT_SUPPORT = 0x07,
		CONN_ADDR_NOT_SUPPORT = 0x08,

		CMD_CONNECT = 0x01
	};

	static size_t makeMethodReq(void *buf,const uint8_t methods[], uint8_t nmethods) {
		
		size_t i = 0;
		uint8_t *bytes = (uint8_t*)buf;
	
		bytes[i++] = VERNO;
		bytes[i++] = nmethods;
		for (uint8_t j = 0; j < nmethods; ++j)
			bytes[i++] = methods[j];
		return i;
	}

	struct MethodReply
	{
		uint8_t VER;
		uint8_t METHOD;

		bool check(size_t size) const {
			return size==sizeof(*this) && VER == VERNO && 
				(METHOD == AUTH_METHOD_NON || METHOD == AUTH_METHOD_PWD);
		}
	};

	static size_t makeAuthReq(void *buf, const char *user, const char *pwd) {
		size_t i = 0;
		uint8_t *bytes = (uint8_t*)buf;
	
		bytes[i++] = AUTH_PASSWORD_VER;

		uint8_t luser = (uint8_t)strlen(user);
		bytes[i++] = luser;
		memcpy(bytes + i, user, luser);
		i += luser;

		uint8_t lpwd = (uint8_t)strlen(pwd);
		bytes[i++] = lpwd;
		memcpy(bytes + i, pwd, lpwd);
		i += lpwd;
		return i;
	}

	struct AuthReply
	{
		uint8_t VER;
		uint8_t STATUS;

		bool check(size_t size) const {
			return size ==sizeof(*this) && VER == AUTH_PASSWORD_VER
				&& STATUS == AUTH_PASSWORD_PASSED;
		}
	};

	static size_t makeConnectReq(void *buf, uint32_t dstIp, uint16_t dstPort) {
		size_t i = 0;
		uint8_t *bytes = (uint8_t*)buf;

		bytes[i++] = VERNO;
		bytes[i++] = CMD_CONNECT;
		bytes[i++] = 0;
		bytes[i++] = ADDRTYPE_IPV4;

		(uint32_t&)bytes[i] = dstIp;
		i += sizeof(dstIp);

		(uint16_t&)bytes[i] = dstPort;
		i += sizeof(dstPort);

		return i;
	}

	
	struct ConnectReply
	{
		bool check(size_t size) const {
			return VER == VERNO && REP == 0;
		}

		uint8_t VER;
		uint8_t REP;
		uint8_t RSV;
		uint8_t ATYP;
		uint32_t BND_ADDR;
		uint16_t BND_PORT;
	};
};

#pragma pack()


#endif
