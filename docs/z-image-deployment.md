# Z-Image 本地部署指南

> 阿里通义 Z-Image：6B 参数文生图模型，8 步出图，约 8–16GB 显存。  
> 官方仓库：[Tongyi-MAI/Z-Image](https://github.com/Tongyi-MAI/Z-Image) | Hugging Face：[Z-Image-Turbo](https://huggingface.co/Tongyi-MAI/Z-Image-Turbo)

---

## 1. 环境要求

| 项目 | 要求 |
|------|------|
| **系统** | Windows / Linux |
| **Python** | 3.9+（推荐 3.10+） |
| **GPU** | NVIDIA，CUDA 支持；**显存约 8 GB 最低，16 GB 推荐** |
| **磁盘** | 约 12–15 GB（模型权重） |

---

## 2. 方式一：Diffusers + Python（推荐后端调用）

适合在服务器上跑推理、供 API 或脚本调用。

### 2.1 创建环境并安装依赖

```bash
# 建议使用虚拟环境
python -m venv venv
source venv/bin/activate   # Windows: venv\Scripts\activate

# PyTorch（按你的 CUDA 版本选，示例为 CUDA 12.4）
pip install torch --index-url https://download.pytorch.org/whl/cu124

# Diffusers 需从源码安装以支持 Z-Image（S3-DiT）
pip install git+https://github.com/huggingface/diffusers
pip install -U huggingface_hub transformers accelerate safetensors
```

### 2.2 推理脚本示例

```python
import torch
from diffusers import ZImagePipeline

# 加载 Turbo 版本（8 步，速度快）
pipe = ZImagePipeline.from_pretrained(
    "Tongyi-MAI/Z-Image-Turbo",
    torch_dtype=torch.bfloat16,  # 或 torch.float16，省显存
    low_cpu_mem_usage=False,
)
pipe.to("cuda")

# 生成图片
prompt = "一只在阳光下睡觉的橘猫，写实风格"
image = pipe(
    prompt=prompt,
    num_inference_steps=8,
    guidance_scale=1.0,  # Turbo 通常用 1.0
).images[0]

image.save("output.png")
```

**显存不足时**：可加 `pipe.enable_model_cpu_offload()` 替代 `pipe.to("cuda")`，部分层放 CPU，降低显存占用。

### 2.3 暴露为 HTTP API（供 MyHub 后端调用）

可用 FastAPI 等包一层 HTTP，供卡片封面生成接口调用，例如：

```python
# 示例：FastAPI
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import base64
from io import BytesIO

app = FastAPI()

@app.on_event("startup")
def load_model():
    global pipe
    pipe = ZImagePipeline.from_pretrained(
        "Tongyi-MAI/Z-Image-Turbo",
        torch_dtype=torch.bfloat16,
    )
    pipe.to("cuda")

class GenerateRequest(BaseModel):
    prompt: str
    steps: int = 8

@app.post("/v1/generate")
def generate(req: GenerateRequest):
    image = pipe(prompt=req.prompt, num_inference_steps=req.steps).images[0]
    buf = BytesIO()
    image.save(buf, format="PNG")
    return {"image_base64": base64.b64encode(buf.getvalue()).decode()}
```

MyHub 后端在「建议封面」流程中请求该服务，拿到 base64 或 URL 后写入 `suggestedCover.imageUrl` 或存对象存储后填 URL。

---

## 3. 方式二：ComfyUI（适合本地调试/出图）

适合在本地机器上可视化出图、调试提示词，不要求写 Python 调用代码。

### 3.1 安装 ComfyUI

- 从 [ComfyUI 官方](https://github.com/comfyanonymous/ComfyUI) 克隆或下载发布包，或通过 [Pinokio](https://pinokio.com/) 等一键安装。

### 3.2 下载 Z-Image-Turbo 模型文件

- 打开：<https://huggingface.co/Comfy-Org/z_image_turbo/tree/main/split_files>（或官方 Tongyi-MAI 提供的 Comfy 用权重）。
- 下载并放入 ComfyUI 对应目录：
  - **主模型（diffusion_model）** → `ComfyUI/models/checkpoints/`
  - **文本编码器（text_encoder）** → `ComfyUI/models/text_encoders/`
  - **VAE** → `ComfyUI/models/vae/`

### 3.3 加载工作流并生成

- 若有官方 Z-Image Turbo 工作流 JSON，放入 `ComfyUI/user/default/workflows/`，在 ComfyUI 中加载。
- 在节点里输入提示词，点击 Run 即可生成。

---

## 4. 方式三：云端 API（无本地显存）

不想占本地 GPU，可直接用阿里云或第三方 API：

- **阿里云 Model Studio**：<https://www.alibabacloud.com/help/en/model-studio/z-image-api-reference>
- **Hugging Face Inference API**、**fal.ai**、**Replicate** 等若提供 Z-Image，可按其文档传 prompt 取图片 URL。

后端只需把「生成封面」请求转发到上述 API，将返回的图片 URL 或 base64 填入卡片草稿的 `suggestedCover.imageUrl` 或存盘后填 URL。

---

## 5. 与 MyHub 卡片封面的衔接

- **输入**：卡片标题、标签或简短描述（可由 LLM 从内容生成一句封面描述）。
- **输出**：图片文件或 URL；写入 `GeneratedCardDraft.suggestedCover`（`kind: "image"`, `imageUrl` 或 base64）。
- **流程**：New Capture 中用户选择「AI 封面」时，后端调 Z-Image（本地服务或云端 API），把结果填回前端展示，用户确认后保存。

---

## 6. 常见问题

| 问题 | 处理 |
|------|------|
| `ZImagePipeline` 找不到 | 确保 `pip install git+https://github.com/huggingface/diffusers` 装的是最新版，Z-Image 需较新 diffusers。 |
| CUDA out of memory | 使用 `pipe.enable_model_cpu_offload()`，或减小分辨率、或使用 `torch.float16`。 |
| 下载模型慢 | 可设置 `HF_ENDPOINT=https://hf-mirror.com` 使用镜像，或从 ModelScope 下载后 `from_pretrained("/本地路径")`。 |

---

## 7. 参考链接

- 官方仓库：<https://github.com/Tongyi-MAI/Z-Image>
- Hugging Face：<https://huggingface.co/Tongyi-MAI/Z-Image-Turbo>
- 博客介绍：<https://tongyi-mai.github.io/Z-Image-blog>
