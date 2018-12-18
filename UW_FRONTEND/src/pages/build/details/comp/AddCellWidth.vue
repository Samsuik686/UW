<template>
  <div>
    <div class="message"><h3>新增规格</h3></div>
    <div class="form-group">
      <div class="row no-gutters pl-3 pr-3">
        <div class="form-group col pr-3">
          <label for="cellWidth">规格</label>
          <input type="text" class="form-control" id="cellWidth" v-model="param.boxType" autocomplete="off">
        </div>
        <div class="form-group col pr-3">
          <label for="srcX">初始位置X坐标</label>
          <input type="text" class="form-control" id="srcX" v-model="param.srcX" autocomplete="off">
        </div>
        <div class="form-group col pr-3">
          <label for="srcY">初始位置Y坐标</label>
          <input type="text" class="form-control" id="srcY" v-model="param.srcY" autocomplete="off">
        </div>
        <div class="form-group col pr-3">
          <label for="srcZ">初始位置Z坐标</label>
          <input type="text" class="form-control" id="srcZ" v-model="param.srcZ" autocomplete="off">
        </div>
      </div>
    </div>
    <div>
      <div style="margin:0 0 10px 20px;">
        <Button type="button" class="btn - btn-primary" @click="isDstAdding = !isDstAdding">新增目标地址</Button>
      </div>
      <div class="dst">
        <ul>
          <li>
            <div>起始坐标</div>
            <div>终止坐标</div>
            <div>操作</div>
          </li>
          <li v-for="(item,index) in param.dst">
            <div>({{item.startX}} , {{item.startY}} , {{item.startZ}})</div>
            <div>({{item.endX}} , {{item.endY}} , {{item.endZ}})</div>
            <div>
              <div class="btn pl-1 pr-1" title="修改" @click="edit(item,index)">
                <icon name="edit" scale="1.8"></icon>
              </div>
              <div class="btn pl-1 pr-1" title="删除" @click="showWarning(item,index)">
                <icon name="cancel" scale="1.8"></icon>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <transition name="fade">
        <div v-if="isDstAdding" id="add-window">
          <add-dst @getAddDstData="getAddDstData"/>
        </div>
      </transition>
      <transition name="fade">
        <div v-if="isDstEditing" id="edit-window">
          <edit-dst @getEditDstData="getEditDstData" :rowData="rowData"/>
        </div>
      </transition>
      <transition name="fade">
        <div v-if="isDstDeleting" id="delete-window">
          <div class="delete-panel">
            <div class="delete-panel-container form-row flex-column justify-content-between">
              <div class="form-row">
                <div class="form-group mb-0">
                  <h3>确认删除：</h3>
                </div>
              </div>
              <div class="form-row w-100">
                <div class="text-center">
                  <p>你正在删除坐标为 "({{rowData.startX}} , {{rowData.startY}} , {{rowData.startZ}}) - ({{rowData.endX}} ,
                    {{rowData.endY}} , {{rowData.endZ}})" 的坐标，请确认是否删除</p>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="form-row justify-content-around">
                <a class="btn btn-secondary col mr-1 text-white" @click="isDstDeleting = false">取消</a>
                <a class="btn btn-danger col ml-1 text-white" @click="submitDelete">确定</a>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <div class="btn-group">
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-secondary ml-3 mr-4" @click="closeAddPanel">取消</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="save">保存</a>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';
  import AddDst from './subscomp/AddDst'
  import EditDst from './subscomp/EditDst'
  export default {
    name: "AddCellWidth",
    components: {
      AddDst,
      EditDst
    },
    mounted() {
      this.resetForm();
      eventBus.$on('closeDstAddPanel',() => {
        this.isDstAdding = false;
      });
      eventBus.$on('closeDstEditPanel', () => {
        this.isDstEditing = false;
      });
    },
    data() {
      return {
        param: {
          boxType: '',
          srcX: '',
          srcY: '',
          srcZ: '',
          dst:[]
        },
        isDstAdding:false,
        isDstEditing: false,
        isDstDeleting: false,
        index:''
      }
    },
    methods: {
      closeAddPanel:function(){
        this.resetForm();
        eventBus.$emit('closeAddPanel');
      },
      resetForm: function () {
        this.param = {
          boxType: '',
          srcX: '',
          srcY: '',
          srcZ: '',
          dst: []
        };
      },
      save: function () {
        for (let item in this.param) {
          if (this.param[item] === '' || this.param[item].length === 0) {
            this.$alertWarning('内容不能为空');
            return;
          }
        }
        for (let item in this.param) {
          if (item !== 'dst') {
            let reg = new RegExp('^[0-9]*[1-9][0-9]*$');
            if (!reg.test(this.param[item])) {
              this.$alertWarning('内容格式不正确，请输入正整数');
              return;
            }
          }
        }
        this.$emit('getAddData',this.param);
      },
      getAddDstData: function (item) {
        for(let i = 0;i<this.param.dst.length;i++){
          if(JSON.stringify(item) === JSON.stringify(this.param.dst[i])){
            this.$alertWarning('不能添加重复的目标地址');
            return;
          }
        }
        this.isDstAdding = false;
        this.param.dst.push(item);
      },
      getEditDstData: function (item, index) {
        delete this.param.dst[index]["index"];
        for(let i = 0;i<this.param.dst.length;i++){
          if(i !== index){
            if(JSON.stringify(item) === JSON.stringify(this.param.dst[i])){
              this.$alertWarning('不能添加重复的目标地址');
              return;
            }
          }
        }
        this.isDstEditing = false;
        this.param.dst.splice(index,1,item);
      },
      edit: function (item, index) {
        this.rowData = item;
        this.rowData["index"] = index;
        this.isDstEditing = true;
      },
      showWarning: function (item, index) {
        this.rowData = item;
        this.index = index;
        this.isDstDeleting = true;
      },
      submitDelete: function () {
        this.param.dst.splice(this.index, 1);
        this.isDstDeleting = false;
      }
    }
  }
</script>

<style scoped>
  .message {
    margin-left: 20px;
    margin-bottom: 30px;
  }

  .btn-group {
    display: flex;
    margin-top: 20px;
    float: right;
  }
  .dst {
    width: calc(100% - 20px);
    min-height: 280px;
    max-height:500px;
    overflow-y: auto;
    border: 1px solid #eee;
    margin-left: 20px;
    border-radius: 8px;
    padding: 10px 20px;
  }

  ul, li {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  li {
    display: flex;
    height: 50px;
    line-height: 50px;
    justify-content: space-around;
  }

  li > div {
    width: 200px;
    text-align: center;
  }

  #add-window {
    z-index: 100;
  }

  #edit-window {
    z-index: 100;
  }

  #delete-window {
    z-index: 100;
  }

  .delete-panel {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 101;
  }

  .delete-panel-container {
    background: #ffffff;
    height: 220px;
    min-width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
