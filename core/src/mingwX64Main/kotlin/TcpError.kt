/**
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.transport.net

import platform.posix.*
import kotlin.native.concurrent.*

actual val EAGAIN_ERROR: Int = EAGAIN

@SharedImmutable
actual val errorsMapping = setOf(
    TcpError(
        name = "WSA_INVALID_HANDLE",
        id = 6,
        description = "Specified event object handle is invalid. An application attempts to use an event object, but the specified handle is not valid."
    ),

    TcpError(
        name = "WSA_NOT_ENOUGH_MEMORY",
        id = 8,
        description = "Insufficient memory available. An application used a Windows Sockets function that directly maps to a Windows function. The Windows function is indicating a lack of required memory resources."
    ),

    TcpError(
        name = "WSA_INVALID_PARAMETER",
        id = 87,
        description = "One or more parameters are invalid. An application used a Windows Sockets function which directly maps to a Windows function. The Windows function is indicating a problem with one or more parameters."
    ),

    TcpError(
        name = "WSA_OPERATION_ABORTED",
        id = 995,
        description = "Overlapped operation aborted. An overlapped operation was canceled due to the closure of the socket, or the execution of the SIO_FLUSH command in ."
    ),

    TcpError(
        name = "WSA_IO_INCOMPLETE",
        id = 996,
        description = "Overlapped I/O event object not in signaled state. The application has tried to determine the status of an overlapped operation which is not yet completed. Applications that use in a polling mode to determine when an overlapped operation has completed, get this error code until the operation is complete."
    ),

    TcpError(
        name = "WSA_IO_PENDING",
        id = 997,
        description = "The application has initiated an overlapped operation that cannot be completed immediately. A completion indication will be given later when the operation has been completed."
    ),
    TcpError(
        name = "WSAEINTR",
        id = 10004,
        description = "Interrupted function call. A blocking operation was interrupted by a call to ."
    ),

    TcpError(
        name = "WSAEBADF",
        id = 10009,
        description = "File handle is not valid. The file handle supplied is not valid. "
    ),

    TcpError(
        name = "WSAEACCES",
        id = 10013,
        description = "Permission denied. An attempt was made to access a socket in a way forbidden by its access permissions. An example is using a broadcast address for  option."
    ),

    TcpError(
        name = "WSAEFAULT",
        id = 10014,
        description = "Bad address. The system detected an invalid pointer address in attempting to use a pointer argument of a call. This error occurs if an application passes an invalid pointer value, or if the length of the buffer is too small. For instance, if the length of an argument, which is a  structure, is smaller than the sizeof(sockaddr),."
    ),

    TcpError(
        name = "WSAEINVAL",
        id = 10022,
        description = "Invalid argument. Some invalid argument was supplied (for example, specifying an invalid level to the  on a socket that is not listening."
    ),

    TcpError(
        name = "WSAEMFILE",
        id = 10024,
        description = "Too many open files. Too many open sockets. Each implementation may have a maximum number of socket handles available, either globally, per process, or per thread."
    ),

    TcpError(
        name = "WSAEWOULDBLOCK",
        id = 10035,
        description = "Resource temporarily unavailable. This error is returned from operations on nonblocking sockets that cannot be completed immediately, for example  on a nonblocking SOCK_STREAM socket, since some time must elapse for the connection to be established."
    ),

    TcpError(
        name = "WSAEINPROGRESS",
        id = 10036,
        description = "Operation now in progress. A blocking operation is currently executing. Windows Sockets only allows a single blocking operation—per- task or thread—to be outstanding, and if any other function call is made (whether or not it references that or any other socket), the function fails with the WSAEINPROGRESS error."
    ),

    TcpError(
        name = "WSAEALREADY",
        id = 10037,
        description = "Operation already in progress. An operation was attempted on a nonblocking socket with an operation already in progress—that is, calling  a second time on a nonblocking socket that is already connecting, or canceling an asynchronous request (<strong>WSAAsyncGetXbyY</strong>), that has already been canceled or completed."
    ),

    TcpError(
        name = "WSAENOTSOCK",
        id = 10038,
        description = "Socket operation on nonsocket. An operation was attempted on something that is not a socket. Either the socket handle parameter did not reference a valid socket, or for , a member of an <strong>fd_set</strong> was not valid."
    ),

    TcpError(
        name = "WSAEDESTADDRREQ",
        id = 10039,
        description = "Destination address required. A required address was omitted from an operation on a socket. For example, this error is returned if  is called with the remote address of ADDR_ANY."
    ),

    TcpError(
        name = "WSAEMSGSIZE",
        id = 10040,
        description = "Message too long. A message sent on a datagram socket was larger than the internal message buffer or some other network limit, or the buffer used to receive a datagram was smaller than the datagram itself."
    ),

    TcpError(
        name = "WSAEPROTOTYPE",
        id = 10041,
        description = "Protocol wrong type for socket. A protocol was specified in the  function call that does not support the semantics of the socket type requested. For example, the ARPA Internet UDP protocol cannot be specified with a socket type of SOCK_STREAM."
    ),

    TcpError(
        name = "WSAENOPROTOOPT",
        id = 10042,
        description = "Bad protocol option. An unknown, invalid or unsupported option or level was specified in a  call."
    ),

    TcpError(
        name = "WSAEPROTONOSUPPORT",
        id = 10043,
        description = "Protocol not supported. The requested protocol has not been configured into the system, or no implementation for it exists. For example, a  call requests a SOCK_DGRAM socket, but specifies a stream protocol."
    ),

    TcpError(
        name = "WSAESOCKTNOSUPPORT",
        id = 10044,
        description = "Socket type not supported. The support for the specified socket type does not exist in this address family. For example, the optional type SOCK_RAW might be selected in a  call, and the implementation does not support SOCK_RAW sockets at all."
    ),

    TcpError(
        name = "WSAEOPNOTSUPP",
        id = 10045,
        description = "Operation not supported. The attempted operation is not supported for the type of object referenced. Usually this occurs when a socket descriptor to a socket that cannot support this operation is trying to accept a connection on a datagram socket."
    ),

    TcpError(
        name = "WSAEPFNOSUPPORT",
        id = 10046,
        description = "Protocol family not supported. The protocol family has not been configured into the system or no implementation for it exists. This message has a slightly different meaning from WSAEAFNOSUPPORT. However, it is interchangeable in most cases, and all Windows Sockets functions that return one of these messages also specify WSAEAFNOSUPPORT."
    ),

    TcpError(
        name = "WSAEAFNOSUPPORT",
        id = 10047,
        description = "Address family not supported by protocol family. An address incompatible with the requested protocol was used. All sockets are created with an associated address family (that is, AF_INET for Internet Protocols), and a generic protocol type (that is, SOCK_STREAM),. This error is returned if an incorrect protocol is explicitly requested in the ."
    ),

    TcpError(
        name = "WSAEADDRINUSE",
        id = 10048,
        description = "Address already in use. Typically, only one usage of each socket address (protocol/IP address/port), is permitted. This error occurs if an application attempts to ."
    ),

    TcpError(
        name = "WSAEADDRNOTAVAIL",
        id = 10049,
        description = "Cannot assign requested address. The requested address is not valid in its context. This normally results from an attempt to  when the remote address or port is not valid for a remote computer (for example, address or port 0),."
    ),

    TcpError(
        name = "WSAENETDOWN",
        id = 10050,
        description = "Network is down. A socket operation encountered a dead network. This could indicate a serious failure of the network system (that is, the protocol stack that the Windows Sockets DLL runs over),, the network interface, or the local network itself."
    ),

    TcpError(
        name = "WSAENETUNREACH",
        id = 10051,
        description = "Network is unreachable. A socket operation was attempted to an unreachable network. This usually means the local software knows no route to reach the remote host."
    ),

    TcpError(
        name = "WSAENETRESET",
        id = 10052,
        description = "Network dropped connection on reset. The connection has been broken due to keep-alive activity detecting a failure while the operation was in progress. It can also be returned by  on a connection that has already failed."
    ),

    TcpError(
        name = "WSAECONNABORTED",
        id = 10053,
        description = "Software caused connection abort. An established connection was aborted by the software in your host computer, possibly due to a data transmission time-out or protocol error."
    ),

    TcpError(
        name = "WSAECONNRESET",
        id = 10054,
        description = "Connection reset by peer. An existing connection was forcibly closed by the remote host. This normally results if the peer application on the remote host is suddenly stopped, the host is rebooted, the host or remote network interface is disabled, or the remote host uses a hard close (see  for more information on the SO_LINGER option on the remote socket),. This error may also result if a connection was broken due to keep-alive activity detecting a failure while one or more operations are in progress. Operations that were in progress fail with WSAENETRESET. Subsequent operations fail with WSAECONNRESET."
    ),

    TcpError(
        name = "WSAENOBUFS",
        id = 10055,
        description = "No buffer space available. An operation on a socket could not be performed because the system lacked sufficient buffer space or because a queue was full."
    ),

    TcpError(
        name = "WSAEISCONN",
        id = 10056,
        description = "Socket is already connected. A connect request was made on an already-connected socket. Some implementations also return this error if  is called on a connected SOCK_DGRAM socket (for SOCK_STREAM sockets, the <em>to</em> parameter in <strong>sendto</strong> is ignored), although other implementations treat this as a legal occurrence."
    ),

    TcpError(
        name = "WSAENOTCONN",
        id = 10057,
        description = "Socket is not connected. A request to send or receive data was disallowed because the socket is not connected and (when sending on a datagram socket using  if the connection has been reset."
    ),

    TcpError(
        name = "WSAESHUTDOWN",
        id = 10058,
        description = "Cannot send after socket shutdown. A request to send or receive data was disallowed because the socket had already been shut down in that direction with a previous  call. By calling <strong>shutdown</strong> a partial close of a socket is requested, which is a signal that sending or receiving, or both have been discontinued."
    ),

    TcpError(
        name = "WSAETOOMANYREFS",
        id = 10059,
        description = "Too many references. Too many references to some kernel object."
    ),

    TcpError(
        name = "WSAETIMEDOUT",
        id = 10060,
        description = "Connection timed out. A connection attempt failed because the connected party did not properly respond after a period of time, or the established connection failed because the connected host has failed to respond."
    ),

    TcpError(
        name = "WSAECONNREFUSED",
        id = 10061,
        description = "Connection refused. No connection could be made because the target computer actively refused it. This usually results from trying to connect to a service that is inactive on the foreign host—that is, one with no server application running."
    ),

    TcpError(name = "WSAELOOP", id = 10062, description = "Cannot translate name. Cannot translate a name."),

    TcpError(
        name = "WSAENAMETOOLONG",
        id = 10063,
        description = "Name too long. A name component or a name was too long."
    ),

    TcpError(
        name = "WSAEHOSTDOWN",
        id = 10064,
        description = "Host is down. A socket operation failed because the destination host is down. A socket operation encountered a dead host. Networking activity on the local host has not been initiated. These conditions are more likely to be indicated by the error WSAETIMEDOUT."
    ),

    TcpError(
        name = "WSAEHOSTUNREACH",
        id = 10065,
        description = "No route to host. A socket operation was attempted to an unreachable host. See WSAENETUNREACH."
    ),

    TcpError(
        name = "WSAENOTEMPTY",
        id = 10066,
        description = "Directory not empty. Cannot remove a directory that is not empty."
    ),

    TcpError(
        name = "WSAEPROCLIM",
        id = 10067,
        description = "Too many processes. A Windows Sockets implementation may have a limit on the number of applications that can use it simultaneously.  may fail with this error if the limit has been reached."
    ),

    TcpError(name = "WSAEUSERS", id = 10068, description = "User quota exceeded. Ran out of user quota. "),

    TcpError(name = "WSAEDQUOT", id = 10069, description = "Disk quota exceeded. Ran out of disk quota. "),

    TcpError(
        name = "WSAESTALE",
        id = 10070,
        description = "Stale file handle reference. The file handle reference is no longer available. "
    ),

    TcpError(name = "WSAEREMOTE", id = 10071, description = "Item is remote. The item is not available locally. "),

    TcpError(
        name = "WSASYSNOTREADY",
        id = 10091,
        description = "Network subsystem is unavailable. This error is returned by  if the Windows Sockets implementation cannot function at this time because the underlying system it uses to provide network services is currently unavailable. Users should check: \nThat the appropriate Windows Sockets DLL file is in the current path\nThat they are not trying to use more than one Windows Sockets implementation simultaneously. If there is more than one Winsock DLL on your system, be sure the first one in the path is appropriate for the network subsystem currently loaded.\nThe Windows Sockets implementation documentation to be sure all necessary components are currently installed and configured correctly."
    ),

    TcpError(
        name = "WSAVERNOTSUPPORTED",
        id = 10092,
        description = "Winsock.dll version out of range. The current Windows Sockets implementation does not support the Windows Sockets specification version requested by the application. Check that no old Windows Sockets DLL files are being accessed."
    ),

    TcpError(
        name = "WSANOTINITIALISED",
        id = 10093,
        description = "Successful WSAStartup not yet performed. Either the application has not called  has been called too many times."
    ),

    TcpError(
        name = "WSAEDISCON",
        id = 10101,
        description = "Graceful shutdown in progress. Returned by  to indicate that the remote party has initiated a graceful shutdown sequence."
    ),

    TcpError(
        name = "WSAENOMORE",
        id = 10102,
        description = "No more results. No more results can be returned by the  function."
    ),

    TcpError(
        name = "WSAECANCELLED",
        id = 10103,
        description = "Call has been canceled. A call to the  function was made while this call was still processing. The call has been canceled."
    ),

    TcpError(
        name = "WSAEINVALIDPROCTABLE",
        id = 10104,
        description = "Procedure call table is invalid. The service provider procedure call table is invalid. A service provider returned a bogus procedure table to Ws2_32.dll. This is usually caused by one or more of the function pointers being <strong>NULL</strong>."
    ),

    TcpError(
        name = "WSAEINVALIDPROVIDER",
        id = 10105,
        description = "Service provider is invalid. The requested service provider is invalid. This error is returned by the  functions if the protocol entry specified could not be found. This error is also returned if the service provider returned a version number other than 2.0."
    ),

    TcpError(
        name = "WSAEPROVIDERFAILEDINIT",
        id = 10106,
        description = "Service provider failed to initialize. The requested service provider could not be loaded or initialized. This error is returned if either a service provider's DLL could not be loaded ( function failed."
    ),

    TcpError(
        name = "WSASYSCALLFAILURE",
        id = 10107,
        description = "System call failure. A system call that should never fail has failed. This is a generic error code, returned under various conditions. <br> Returned when a system call that should never fail does fail. For example, if a call to  fails or one of the registry functions fails trying to manipulate the protocol/namespace catalogs.<br> Returned when a provider does not return SUCCESS and does not provide an extended error code. Can indicate a service provider implementation error."
    ),

    TcpError(
        name = "WSASERVICE_NOT_FOUND",
        id = 10108,
        description = "Service not found. No such service is known. The service cannot be found in the specified name space."
    ),

    TcpError(
        name = "WSATYPE_NOT_FOUND",
        id = 10109,
        description = "Class type not found. The specified class was not found."
    ),

    TcpError(
        name = "WSA_E_NO_MORE",
        id = 10110,
        description = "No more results. No more results can be returned by the  function."
    ),

    TcpError(
        name = "WSA_E_CANCELLED",
        id = 10111,
        description = "Call was canceled. A call to the  function was made while this call was still processing. The call has been canceled."
    ),

    TcpError(
        name = "WSAEREFUSED",
        id = 10112,
        description = "Database query was refused. A database query failed because it was actively refused."
    ),

    TcpError(
        name = "WSAHOST_NOT_FOUND",
        id = 11001,
        description = "Host not found. No such host is known. The name is not an official host name or alias, or it cannot be found in the database(s), being queried. This error may also be returned for protocol and service queries, and means that the specified name could not be found in the relevant database."
    ),

    TcpError(
        name = "WSATRY_AGAIN",
        id = 11002,
        description = "Nonauthoritative host not found. This is usually a temporary error during host name resolution and means that the local server did not receive a response from an authoritative server. A retry at some time later may be successful."
    ),

    TcpError(
        name = "WSANO_RECOVERY",
        id = 11003,
        description = "This is a nonrecoverable error. This indicates that some sort of nonrecoverable error occurred during a database lookup. This may be because the database files (for example, BSD-compatible HOSTS, SERVICES, or PROTOCOLS files), could not be found, or a DNS request was returned by the server with a severe error."
    ),

    TcpError(
        name = "WSANO_DATA",
        id = 11004,
        description = "Valid name, no data record of requested type. The requested name is valid and was found in the database, but it does not have the correct associated data being resolved for. The usual example for this is a host name-to-address translation attempt (using ), which uses the DNS (Domain Name Server),. An MX record is returned but no A record—indicating the host itself exists, but is not directly reachable."
    ),

    TcpError(
        name = "WSA_QOS_RECEIVERS",
        id = 11005,
        description = "QoS receivers. At least one QoS reserve has arrived."
    ),

    TcpError(
        name = "WSA_QOS_SENDERS",
        id = 11006,
        description = "QoS senders. At least one QoS send path has arrived."
    ),

    TcpError(name = "WSA_QOS_NO_SENDERS", id = 11007, description = "No QoS senders. There are no QoS senders."),

    TcpError(name = "WSA_QOS_NO_RECEIVERS", id = 11008, description = "QoS no receivers. There are no QoS receivers."),

    TcpError(
        name = "WSA_QOS_REQUEST_CONFIRMED",
        id = 11009,
        description = "QoS request confirmed. The QoS reserve request has been confirmed."
    ),

    TcpError(
        name = "WSA_QOS_ADMISSION_FAILURE",
        id = 11010,
        description = "QoS admission error. A QoS error occurred due to lack of resources."
    ),

    TcpError(
        name = "WSA_QOS_POLICY_FAILURE",
        id = 11011,
        description = "QoS policy failure. The QoS request was rejected because the policy system couldn't allocate the requested resource within the existing policy. "
    ),

    TcpError(
        name = "WSA_QOS_BAD_STYLE",
        id = 11012,
        description = "QoS bad style. An unknown or conflicting QoS style was encountered."
    ),

    TcpError(
        name = "WSA_QOS_BAD_OBJECT",
        id = 11013,
        description = "QoS bad object. A problem was encountered with some part of the filterspec or the provider-specific buffer in general."
    ),

    TcpError(
        name = "WSA_QOS_TRAFFIC_CTRL_ERROR",
        id = 11014,
        description = "QoS traffic control error. An error with the underlying traffic control (TC), API as the generic QoS request was converted for local enforcement by the TC API. This could be due to an out of memory error or to an internal QoS provider error. "
    ),

    TcpError(name = "WSA_QOS_GENERIC_ERROR", id = 11015, description = "QoS generic error. A general QoS error."),

    TcpError(
        name = "WSA_QOS_ESERVICETYPE",
        id = 11016,
        description = "QoS service type error. An invalid or unrecognized service type was found in the QoS flowspec."
    ),

    TcpError(
        name = "WSA_QOS_EFLOWSPEC",
        id = 11017,
        description = "QoS flowspec error. An invalid or inconsistent flowspec was found in the  structure."
    ),

    TcpError(
        name = "WSA_QOS_EPROVSPECBUF",
        id = 11018,
        description = "Invalid QoS provider buffer. An invalid QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_EFILTERSTYLE",
        id = 11019,
        description = "Invalid QoS filter style. An invalid QoS filter style was used."
    ),

    TcpError(
        name = "WSA_QOS_EFILTERTYPE",
        id = 11020,
        description = "Invalid QoS filter type. An invalid QoS filter type was used."
    ),

    TcpError(
        name = "WSA_QOS_EFILTERCOUNT",
        id = 11021,
        description = "Incorrect QoS filter count. An incorrect number of QoS FILTERSPECs were specified in the FLOWDESCRIPTOR."
    ),

    TcpError(
        name = "WSA_QOS_EOBJLENGTH",
        id = 11022,
        description = "Invalid QoS object length. An object with an invalid ObjectLength field was specified in the QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_EFLOWCOUNT",
        id = 11023,
        description = "Incorrect QoS flow count. An incorrect number of flow descriptors was specified in the QoS structure."
    ),

    TcpError(
        name = "WSA_QOS_EUNKOWNPSOBJ",
        id = 11024,
        description = "Unrecognized QoS object. An unrecognized object was found in the QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_EPOLICYOBJ",
        id = 11025,
        description = "Invalid QoS policy object. An invalid policy object was found in the QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_EFLOWDESC",
        id = 11026,
        description = "Invalid QoS flow descriptor. An invalid QoS flow descriptor was found in the flow descriptor list."
    ),

    TcpError(
        name = "WSA_QOS_EPSFLOWSPEC",
        id = 11027,
        description = "Invalid QoS provider-specific flowspec. An invalid or inconsistent flowspec was found in the QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_EPSFILTERSPEC",
        id = 11028,
        description = "Invalid QoS provider-specific filterspec. An invalid FILTERSPEC was found in the QoS provider-specific buffer."
    ),

    TcpError(
        name = "WSA_QOS_ESDMODEOBJ",
        id = 11029,
        description = "Invalid QoS shape discard mode object. An invalid shape discard mode object was found in the QoS provider-specific buffer."
    ),

    TcpError(
        id = 11030,
        name = "WSA_QOS_ESHAPERATEOBJ",
        description = "Invalid QoS shaping rate object. An invalid shaping rate object was found in the QoS provider-specific buffer."
    ),

    TcpError(
        id = 11031,
        name = "WSA_QOS_RESERVED_PETYPE",
        description = "Reserved policy QoS element type. A reserved policy element was found in the QoS provider-specific buffer."
    )
).associateBy { it.id }