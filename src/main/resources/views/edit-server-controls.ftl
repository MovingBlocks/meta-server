
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputName">Server Name</label>
    <div class="col-sm-10">
      <input type="text" name="name" value="${name}" class="form-control" id="inputName" placeholder="My Server">
    </div>
  </div>
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputAddress">Address</label>
    <div class="col-sm-6">
      <input type="text" name="address" value="${address}" class="form-control" id="inputAddress" placeholder="myGameServer.org" <#if disabled??>readonly</#if>>
    </div>
    <label class="col-sm-2 control-label" for="inputPort">Port</label>
    <div class="col-sm-2">
      <input type="number" name="port" value="${port?c}" class="form-control" id="inputPort" placeholder="25777" <#if disabled??>readonly</#if>>
    </div>
  </div>
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputOwner">Owner</label>
    <div class="col-sm-10">
      <input type="text" name="owner" value="${owner}" class="form-control" id="inputOwner" placeholder="MyAccountName">
    </div>
  </div>  
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputSecret">Secret</label>
    <div class="col-sm-10">
      <input type="password" name="secret" class="form-control" id="inputSecret" placeholder="secret key">
    </div>
  </div>
  <div class="form-group">
    <label class="col-sm-2 control-label" for="inputActive">Active</label>
    <div class="col-sm-10">
       <input type="checkbox" name="active" id="inputActive" style="zoom:1.5" <#if active>checked="checked"</#if>>
    </div>
  </div>
