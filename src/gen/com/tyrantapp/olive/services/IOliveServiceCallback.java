/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/onetop21/workspace/Olive/src/com/tyrantapp/olive/services/IOliveServiceCallback.aidl
 */
package com.tyrantapp.olive.services;
public interface IOliveServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.tyrantapp.olive.services.IOliveServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.tyrantapp.olive.services.IOliveServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.tyrantapp.olive.services.IOliveServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.tyrantapp.olive.services.IOliveServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.tyrantapp.olive.services.IOliveServiceCallback))) {
return ((com.tyrantapp.olive.services.IOliveServiceCallback)iin);
}
return new com.tyrantapp.olive.services.IOliveServiceCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onResult:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onResult(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.tyrantapp.olive.services.IOliveServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onResult(int nErroCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nErroCode);
mRemote.transact(Stub.TRANSACTION_onResult, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onResult(int nErroCode) throws android.os.RemoteException;
}
