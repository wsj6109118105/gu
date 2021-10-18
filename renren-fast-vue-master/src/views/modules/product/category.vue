<!--  -->
<template>
  <div>
    <el-switch v-model="draggable" active-text="开启拖拽"> </el-switch>
    <el-button
      v-if="draggable"
      @click="save"
      type="success"
      icon="el-icon-check"
      circle
      size="mini"
    ></el-button>
    <el-button
      type="danger"
      icon="el-icon-delete"
      circle
      size="mini"
      @click="deleteCategory"
    ></el-button>
    <el-tree
      show-checkbox
      node-key="catId"
      :data="menu"
      :props="defaultProps"
      @node-click="handleNodeClick"
      @node-drop="handleDrop"
      :draggable="draggable"
      :expand-on-click-node="false"
      :default-expanded-keys="expandedKey"
      :allow-drop="allowDrop"
      ref="menuTree"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            Append
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            Edit
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="菜单名称" :label-width="formLabelWidth">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标" :label-width="formLabelWidth">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位" :label-width="formLabelWidth">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';

export default {
  //import引入的组件需要注入到对象中才能使用
  components: {},
  data() {
    return {
      pCid: [],
      draggable: false,
      updatedNodes: [],
      maxLevel: 0,
      title: "",
      expandedKey: [],
      dialogType: "",
      dialogVisible: false,
      updialogVisible: false,
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        catId: null,
        icon: "",
        productUnit: "",
      },
      menu: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
    };
  },
  methods: {
    handleNodeClick(menu) {
      //console.log(menu);
    },
    getDataList() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        console.log("成功获取到菜单数据...", data.data);
        this.menu = data.data;
      });
    },
    append(data) {
      this.dialogVisible = true;
      console.log(data);
      this.dialogType = "add";
      this.title = "添加菜单";
      this.category.name = "";
      this.category.icon = "";
      this.category.productUnit = "";
      this.category.catId = null;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
    },
    addCategory() {
      console.log("提交的数据", this.category);
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          type: "success",
          message: "添加成功!",
        });
        this.dialogVisible = false;
        this.getDataList();
        this.expandedKey = [this.category.parentCid];
      });
    },
    submitData() {
      if (this.dialogType == "add") {
        this.addCategory();
      }
      if (this.dialogType == "edit") {
        this.editCategory();
      }
    },
    allowDrop(draggingNode, dropNode, type) {
      //分别为当前节点，要去的节点，以及位置关系
      //1 判断当前节点总层数
      this.maxLevel = 0;
      this.countNodeLevel(draggingNode);
      let level = this.maxLevel - draggingNode.level + 1;
      console.log(level, dropNode.level, this.maxLevel);
      if (type == "inner") {
        return level + dropNode.level <= 3;
      } else {
        return level + dropNode.parent.level <= 3;
      }
    },
    countNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level;
          }
          this.countNodeLevel(node.childNodes[i]);
        }
      }
    },
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log("tree drop: ", draggingNode, dropNode, dropType);
      //当前节点最新父节点的id
      let pCid = 0;
      let siblings = null;
      if (dropType == "after" || dropType == "before") {
        pCid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        siblings = dropNode.parent.childNodes;
      } else {
        pCid = dropNode.data.catId;
        siblings = dropNode.childNodes;
      }
      //当前拖拽节点的最新排序
      for (let i = 0; i < siblings.length; i++) {
        if (siblings[i].data.catId == draggingNode.data.catId) {
          let newLevel = draggingNode.level;
          if (siblings[i].level != draggingNode.level) {
            newLevel = siblings[i].level;
            //修改子节点的层级
            this.updateChildNodeLevel(siblings[i]);
          }

          this.updatedNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: newLevel,
          });
        } else {
          this.updatedNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }
      console.log(siblings);
      console.log(this.updatedNodes);
      this.pCid.push(pCid);
      //当前拖拽节点的最新层级
    },
    updateChildNodeLevel(node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          var cNode = node.childNodes[i].data;
          this.updatedNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level,
          });
          this.updateChildNodeLevel(node.childNodes[i]);
        }
      }
    },
    save() {
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updatedNodes, false),
      }).then(({ data }) => {
        this.$message({
          type: "success",
          message: "菜单修改成功!",
        });
        this.getDataList();
        this.expandedKey = this.pCid;
        (this.updatedNodes = []), (this.maxLevel = 0);
      });
    },
    deleteCategory() {
      let checkNodes = this.$refs.menuTree.getCheckedNodes();
      console.log(checkNodes);
      let catIds = [];
      for (let i = 0; i < checkNodes.length; i++) {
        catIds.push(checkNodes[i].catId);
      }
      console.log(catIds);
      this.$confirm(`是否删除菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(catIds, false),
          }).then(({ data }) => {
            this.$message({
              type: "success",
              message: "删除成功!",
            });
            this.getDataList();
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
    edit(data) {
      console.log("要修改的数据：", data);
      this.dialogVisible = true;
      this.dialogType = "edit";
      this.title = "修改菜单";
      //发送请求获取当前节点最新的数据，然后回显
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
      }).then(({ data }) => {
        //请求成功
        console.log("要回显的数据", data);
        this.category.name = data.data.name;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.catId = data.data.catId;
        this.category.parentCid = data.data.parentCid;
      });
    },
    editCategory() {
      //console.log("要提交的数据", this.category);
      let { name, icon, productUnit, catId } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ name, icon, productUnit, catId }, false),
      }).then(({ data }) => {
        this.$message({
          type: "success",
          message: "修改成功!",
        });
        this.dialogVisible = false;
        this.getDataList();
        this.expandedKey = [this.category.parentCid];
        console.log(this.expandedKey);
      });
    },
    remove(node, data) {
      let ids = [data.catId];
      this.$confirm(`是否删除[${data.name}]菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            console.log("删除成功");
          });
          const parent = node.parent;
          const children = parent.data.children || parent.data;
          const index = children.findIndex((d) => d.id === data.id);
          children.splice(index, 1);
          this.$message({
            type: "success",
            message: "删除成功!",
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
  },
  //监听属性 类似于data概念
  computed: {},
  //监控data中的数据变化
  watch: {},
  //方法集合
  //生命周期 - 创建完成（可以访问当前this实例）
  created() {},
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {
    this.getDataList();
  }, //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>

<style scoped>
</style>