android-LdapCallResolver
========================

This application will contact an LDAP server upon call and will try
to search the number in the LDAP directory, if found it will display a custom toast
message with the result.

The user can build rewrite rules for the numbers so that different types of 
calls (from inside your company PBX, roaming, etc) can be normalized and then found in the LDAP database.

The service also implements a local cache.

And it can also be tested to check against an ldap server on real time.


Jose Carlos Luna Duran
